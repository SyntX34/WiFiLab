package com.syntx34.wifilab.data.network

import com.syntx34.wifilab.domain.model.AuditProgress
import com.syntx34.wifilab.domain.model.AuthorizedNetwork
import com.syntx34.wifilab.domain.model.DeviceResponseKind
import com.syntx34.wifilab.domain.model.DiscoveredDevice
import com.syntx34.wifilab.domain.repository.NetworkAuditService
import com.syntx34.wifilab.domain.security.SubnetCalculator
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

@Singleton
class AndroidNetworkAuditor @Inject constructor() : NetworkAuditService {

    override suspend fun audit(
        network: AuthorizedNetwork,
        onProgress: (AuditProgress) -> Unit,
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        if (!SubnetCalculator.isPrivateIpv4(network.networkAddress) ||
            network.prefixLength !in SubnetCalculator.MIN_PREFIX..SubnetCalculator.MAX_PREFIX
        ) {
            throw IllegalArgumentException("The requested range is outside the authorized private scope")
        }
        val hosts = SubnetCalculator.hostAddresses(network.networkAddress, network.prefixLength)
            .filterNot { it == network.localIpv4 }
        if (hosts.isEmpty()) {
            throw IllegalArgumentException("The requested range has no usable hosts")
        }
        val gateway = network.gatewayIpv4
        val concurrency = Semaphore(MAX_CONCURRENT_HOSTS)
        val completed = AtomicInteger(0)
        val total = hosts.size
        val now = System.currentTimeMillis()
        onProgress(AuditProgress(0, total, null))

        val devices = coroutineScope {
            hosts.map { host ->
                async {
                    concurrency.withPermit {
                        val device = inspectHost(host, gateway, now)
                        val done = completed.incrementAndGet()
                        onProgress(AuditProgress(done, total, host))
                        device
                    }
                }
            }.awaitAll().filterNotNull()
        }
        devices.sortedWith(compareBy<DiscoveredDevice> { ipv4SortKey(it.ipAddress) })
    }

    private fun inspectHost(
        ipAddress: String,
        gateway: String?,
        firstSeenAt: Long,
    ): DiscoveredDevice? {
        val address = runCatching { InetAddress.getByName(ipAddress) }.getOrNull() ?: return null
        val reachableByPing = runCatching { address.isReachable(ICMP_TIMEOUT_MS) }.getOrDefault(false)
        val openPorts = mutableListOf<Int>()
        val serviceNames = mutableListOf<String>()
        for (probe in SERVICE_PROBES) {
            if (connect(address, probe.port)) {
                openPorts += probe.port
                serviceNames += probe.label
            }
        }
        val isGateway = gateway != null && gateway == ipAddress
        if (!reachableByPing && openPorts.isEmpty() && !isGateway) return null
        val responseKind = when {
            isGateway -> DeviceResponseKind.GATEWAY
            openPorts.isNotEmpty() -> DeviceResponseKind.SERVICE
            reachableByPing -> DeviceResponseKind.PING
            else -> DeviceResponseKind.UNKNOWN
        }
        return DiscoveredDevice(
            ipAddress = ipAddress,
            hostname = null,
            serviceNames = serviceNames.distinct(),
            openPorts = openPorts.distinct().sorted(),
            isGateway = isGateway,
            responseKind = responseKind,
            firstSeenAt = firstSeenAt,
        )
    }

    private fun connect(address: InetAddress, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, port), SOCKET_TIMEOUT_MS)
            }
            true
        } catch (error: Exception) {
            false
        }
    }

    private fun ipv4SortKey(address: String): Long =
        SubnetCalculator.ipv4ToLong(address) ?: Long.MAX_VALUE

    private data class ServiceProbe(val port: Int, val label: String)

    companion object {
        private const val MAX_CONCURRENT_HOSTS = 24
        private const val ICMP_TIMEOUT_MS = 300
        private const val SOCKET_TIMEOUT_MS = 250
        private val SERVICE_PROBES = listOf(
            ServiceProbe(80, "HTTP administration"),
            ServiceProbe(443, "HTTPS administration"),
            ServiceProbe(22, "SSH"),
            ServiceProbe(53, "DNS"),
            ServiceProbe(445, "SMB file sharing"),
            ServiceProbe(554, "RTSP camera stream"),
            ServiceProbe(8080, "HTTP alternate"),
            ServiceProbe(8443, "HTTPS alternate"),
        )
    }
}
