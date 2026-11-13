package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.BaselineDevice
import com.syntx34.wifilab.domain.model.DiscoveredDevice

data class BaselineComparison(
    val unexpectedDevices: List<DiscoveredDevice>,
    val knownDevices: List<DiscoveredDevice>,
    val baselineUsed: Boolean,
)

object BaselineComparator {

    fun compare(
        discovered: List<DiscoveredDevice>,
        baseline: List<BaselineDevice>,
    ): BaselineComparison {
        if (baseline.isEmpty()) {
            return BaselineComparison(
                unexpectedDevices = emptyList(),
                knownDevices = discovered,
                baselineUsed = false,
            )
        }
        val trustedAddresses = baseline.map { it.ipAddress }.toSet()
        val trustedIdentities = baseline.map { it.identity }.toSet()
        val unexpected = discovered.filter { device ->
            device.ipAddress !in trustedAddresses && device.identity !in trustedIdentities
        }
        val known = discovered.filterNot { it in unexpected }
        return BaselineComparison(
            unexpectedDevices = unexpected,
            knownDevices = known,
            baselineUsed = true,
        )
    }
}
