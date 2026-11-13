package com.syntx34.wifilab.data.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.syntx34.wifilab.domain.model.ConnectionSnapshot
import com.syntx34.wifilab.domain.model.ConnectionState
import com.syntx34.wifilab.domain.model.LocalNetworkContext
import com.syntx34.wifilab.domain.model.ScanOutcome
import com.syntx34.wifilab.domain.model.WifiNetwork
import com.syntx34.wifilab.domain.repository.WifiRepository
import com.syntx34.wifilab.domain.security.WifiCapabilityParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet4Address
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class AndroidWifiRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : WifiRepository {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val connectivityManager: ConnectivityManager? =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    @Volatile
    private var lastCachedNetworks: List<WifiNetwork> = emptyList()

    @Volatile
    private var lastScanRequestAt: Long = 0L

    override fun observeConnection(): Flow<ConnectionSnapshot> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(ConnectionSnapshot.unavailable)
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(Unit)
            }

            override fun onLost(network: Network) {
                trySend(Unit)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(Unit)
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties,
            ) {
                trySend(Unit)
            }
        }
        try {
            manager.registerDefaultNetworkCallback(callback)
            trySend(Unit)
        } catch (error: SecurityException) {
            trySend(Unit)
        }
        awaitClose {
            runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }.conflate().map { readConnectionSnapshot() }

    override suspend fun scan(): ScanOutcome = withContext(Dispatchers.IO) {
        val manager = wifiManager
            ?: return@withContext ScanOutcome.Unsupported("This device does not expose a Wi-Fi manager")
        val missingPermissions = missingWifiPermissions()
        if (missingPermissions.isNotEmpty()) {
            return@withContext ScanOutcome.PermissionRequired(missingPermissions)
        }
        if (!manager.isWifiEnabled) {
            return@withContext ScanOutcome.Failure("Wi-Fi is turned off. Enable it and scan again.")
        }
        val now = System.currentTimeMillis()
        val elapsed = now - lastScanRequestAt
        if (elapsed in 0 until MIN_REQUEST_INTERVAL_MS) {
            return@withContext ScanOutcome.Throttled(MIN_REQUEST_INTERVAL_MS - elapsed)
        }
        lastScanRequestAt = now

        return@withContext when (val event = awaitScan(manager)) {
            is ScanEvent.Results -> {
                val networks = event.results
                    .map(ScanResultMapper::map)
                    .sortedWith(
                        compareByDescending<WifiNetwork> { it.rssi }
                            .thenBy { it.displayName.lowercase() },
                    )
                lastCachedNetworks = networks
                ScanOutcome.Success(networks, System.currentTimeMillis())
            }

            ScanEvent.Throttled, ScanEvent.Timeout -> {
                /* Android refused a fresh scan, so serve the system cache instead of nothing. */
                val cached = readCachedResults(manager)
                if (cached.isNotEmpty()) {
                    lastCachedNetworks = cached
                    ScanOutcome.Success(cached, System.currentTimeMillis())
                } else if (event == ScanEvent.Throttled) {
                    ScanOutcome.Throttled(MIN_REQUEST_INTERVAL_MS)
                } else {
                    ScanOutcome.Failure(
                        "No scan results arrived. Check that Wi-Fi and location services are on, then try again.",
                    )
                }
            }

            is ScanEvent.PermissionDenied -> ScanOutcome.Failure(
                event.reason ?: "Android denied Wi-Fi scan access. Ensure Location services and permissions are enabled.",
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun readCachedResults(manager: WifiManager): List<WifiNetwork> =
        runCatching { manager.scanResults }
            .getOrDefault(emptyList())
            .map(ScanResultMapper::map)
            .sortedWith(
                compareByDescending<WifiNetwork> { it.rssi }
                    .thenBy { it.displayName.lowercase() },
            )

    @SuppressLint("MissingPermission")
    private suspend fun awaitScan(manager: WifiManager): ScanEvent =
        suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean(false)
            val handler = Handler(Looper.getMainLooper())
            lateinit var receiver: BroadcastReceiver

            fun cleanup() {
                handler.removeCallbacksAndMessages(null)
                runCatching { context.unregisterReceiver(receiver) }
            }

            fun finish(event: ScanEvent) {
                if (!finished.compareAndSet(false, true)) return
                cleanup()
                if (continuation.isActive) {
                    continuation.resume(event)
                }
            }

            receiver = object : BroadcastReceiver() {
                override fun onReceive(receiverContext: Context, intent: Intent) {
                    if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return
                    val results = runCatching { manager.scanResults }.getOrDefault(emptyList())
                    finish(ScanEvent.Results(results))
                }
            }

            val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            try {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
            } catch (error: SecurityException) {
                finish(ScanEvent.PermissionDenied(error.message))
                return@suspendCancellableCoroutine
            }

            val started = try {
                manager.startScan()
            } catch (error: SecurityException) {
                finish(ScanEvent.PermissionDenied(error.message))
                return@suspendCancellableCoroutine
            }
            if (!started) {
                finish(ScanEvent.Throttled)
                return@suspendCancellableCoroutine
            }

            handler.postDelayed({ finish(ScanEvent.Timeout) }, SCAN_TIMEOUT_MS)
            continuation.invokeOnCancellation { finish(ScanEvent.Timeout) }
        }

    override fun cachedNetworks(): List<WifiNetwork> = lastCachedNetworks

    override fun localNetworkContext(): LocalNetworkContext? {
        val manager = connectivityManager ?: return null
        val network = manager.activeNetwork ?: return null
        val properties = manager.getLinkProperties(network) ?: return null
        val local = properties.linkAddresses
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }
            ?: return null
        val gateway = properties.routes
            .filter { it.isDefaultRoute }
            .mapNotNull { it.gateway }
            .filterIsInstance<Inet4Address>()
            .firstOrNull()
        val prefix = properties.linkAddresses
            .firstOrNull { it.address == local }
            ?.prefixLength
        return LocalNetworkContext(
            localIpv4 = local.hostAddress.orEmpty(),
            gatewayIpv4 = gateway?.hostAddress,
            prefixLength = prefix,
            interfaceName = properties.interfaceName,
        )
    }

    @Suppress("DEPRECATION")
    private fun readConnectionSnapshot(): ConnectionSnapshot {
        val manager = connectivityManager ?: return ConnectionSnapshot.unavailable
        val network = manager.activeNetwork ?: return ConnectionSnapshot.unavailable.copy(
            state = ConnectionState.DISCONNECTED,
        )
        val capabilities = manager.getNetworkCapabilities(network) ?: return ConnectionSnapshot.unavailable
        val properties = manager.getLinkProperties(network)
        val wifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val local = properties?.linkAddresses
            ?.map { it.address }
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull { !it.isLoopbackAddress }
        val gateway = properties?.routes
            ?.filter { it.isDefaultRoute }
            ?.mapNotNull { it.gateway }
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull()
        val dns = properties?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
        val info = if (wifi) readWifiInfo() else null
        val bssid = info?.bssid?.takeIf { it != "02:00:00:00:00:00" && it != "00:00:00:00:00:00" }
        val matchingScan = lastCachedNetworks.firstOrNull {
            it.bssid != null && bssid != null && it.bssid.equals(bssid, ignoreCase = true)
        }
        val security = matchingScan?.security
        val ssid = info?.ssid
            ?.trim('"')
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" && it != "0x" }
        return ConnectionSnapshot(
            state = ConnectionState.CONNECTED,
            ssid = ssid,
            bssid = bssid,
            frequencyMhz = info?.frequency?.takeIf { it > 0 },
            rssi = info?.rssi?.takeIf { it < 0 },
            signalLevel = info?.rssi?.takeIf { it < 0 }?.let(WifiCapabilityParser::signalLevelFromRssi),
            linkSpeedMbps = info?.linkSpeed?.takeIf { it > 0 },
            localIpv4 = local?.hostAddress,
            gatewayIpv4 = gateway?.hostAddress,
            dnsServers = dns,
            security = security,
            isWifi = wifi,
            isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
            interfaceName = properties?.interfaceName,
        )
    }

    @Suppress("DEPRECATION")
    private fun readWifiInfo(): WifiInfo? = runCatching {
        wifiManager?.connectionInfo
    }.getOrNull()

    private fun missingWifiPermissions(): List<String> {
        val required = requiredWifiPermissions()
        return required.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredWifiPermissions(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    private sealed interface ScanEvent {
        data class Results(val results: List<ScanResult>) : ScanEvent
        data object Throttled : ScanEvent
        data object Timeout : ScanEvent
        data class PermissionDenied(val reason: String?) : ScanEvent
    }

    companion object {
        private const val MIN_REQUEST_INTERVAL_MS = 12_000L
        private const val SCAN_TIMEOUT_MS = 15_000L
    }
}
