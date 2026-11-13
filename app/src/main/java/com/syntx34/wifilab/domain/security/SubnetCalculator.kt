package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.AuthorizedNetwork

object SubnetCalculator {

    const val MIN_PREFIX = 24
    const val MAX_PREFIX = 30
    const val DEFAULT_PREFIX = 24

    fun ipv4ToLong(address: String): Long? {
        val parts = address.trim().split('.')
        if (parts.size != 4) return null
        var result = 0L
        for (part in parts) {
            if (part.isEmpty() || part.length > 3) return null
            val value = part.toIntOrNull() ?: return null
            if (value !in 0..255) return null
            result = (result shl 8) or value.toLong()
        }
        return result
    }

    fun longToIpv4(value: Long): String {
        val normalized = value and 0xFFFFFFFFL
        return listOf(
            (normalized ushr 24) and 0xFF,
            (normalized ushr 16) and 0xFF,
            (normalized ushr 8) and 0xFF,
            normalized and 0xFF,
        ).joinToString(".")
    }

    fun networkAddress(ip: String, prefixLength: Int): String? {
        val ipValue = ipv4ToLong(ip) ?: return null
        val mask = mask(prefixLength) ?: return null
        return longToIpv4(ipValue and mask)
    }

    fun mask(prefixLength: Int): Long? {
        if (prefixLength !in MIN_PREFIX..MAX_PREFIX) return null
        return (0xFFFFFFFFL shl (32 - prefixLength)) and 0xFFFFFFFFL
    }

    fun isPrivateIpv4(address: String): Boolean {
        val value = ipv4ToLong(address) ?: return false
        val first = (value ushr 24) and 0xFF
        val second = (value ushr 16) and 0xFF
        return first == 10L ||
            (first == 172L && second in 16L..31L) ||
            (first == 192L && second == 168L)
    }

    fun fromLocalAddress(
        localIpv4: String,
        gatewayIpv4: String?,
        prefixLength: Int?,
        interfaceName: String?,
    ): AuthorizedNetwork? {
        if (!isPrivateIpv4(localIpv4)) return null
        val safePrefix = (prefixLength ?: DEFAULT_PREFIX).coerceIn(MIN_PREFIX, MAX_PREFIX)
        val networkAddress = networkAddress(localIpv4, safePrefix) ?: return null
        return AuthorizedNetwork(
            localIpv4 = localIpv4,
            gatewayIpv4 = gatewayIpv4,
            networkAddress = networkAddress,
            prefixLength = safePrefix,
            interfaceName = interfaceName,
        )
    }

    fun parseCidr(cidr: String): Result<AuthorizedNetwork> {
        val trimmed = cidr.trim()
        val parts = trimmed.split('/')
        if (parts.size != 2) {
            return Result.failure(IllegalArgumentException("Use IPv4 CIDR notation such as 192.168.1.0/24"))
        }
        val ipValue = ipv4ToLong(parts[0])
            ?: return Result.failure(IllegalArgumentException("Enter a valid IPv4 address"))
        val prefix = parts[1].toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Prefix length must be a number"))
        if (prefix !in MIN_PREFIX..MAX_PREFIX) {
            return Result.failure(
                IllegalArgumentException("Authorized scans are limited to /$MIN_PREFIX through /$MAX_PREFIX"),
            )
        }
        if (!isPrivateIpv4(parts[0])) {
            return Result.failure(IllegalArgumentException("Only RFC 1918 private networks can be audited"))
        }
        val maskValue = mask(prefix)!!
        val networkValue = ipValue and maskValue
        if (networkValue != ipValue) {
            return Result.failure(
                IllegalArgumentException(
                    "Use the network address ${longToIpv4(networkValue)}/$prefix for this range",
                ),
            )
        }
        return Result.success(
            AuthorizedNetwork(
                localIpv4 = parts[0],
                gatewayIpv4 = null,
                networkAddress = longToIpv4(networkValue),
                prefixLength = prefix,
                interfaceName = null,
            ),
        )
    }

    fun hostAddresses(networkAddress: String, prefixLength: Int): List<String> {
        val networkValue = ipv4ToLong(networkAddress) ?: return emptyList()
        if (prefixLength !in MIN_PREFIX..MAX_PREFIX) return emptyList()
        val hostCount = (1L shl (32 - prefixLength)) - 2
        if (hostCount <= 0) return emptyList()
        return (1L..hostCount).map { longToIpv4(networkValue + it) }
    }

    fun broadcastAddress(networkAddress: String, prefixLength: Int): String? {
        val networkValue = ipv4ToLong(networkAddress) ?: return null
        if (prefixLength !in MIN_PREFIX..MAX_PREFIX) return null
        val hostBits = 32 - prefixLength
        val broadcast = networkValue or ((1L shl hostBits) - 1)
        return longToIpv4(broadcast)
    }

    fun hostCount(prefixLength: Int): Int {
        if (prefixLength !in MIN_PREFIX..MAX_PREFIX) return 0
        return ((1L shl (32 - prefixLength)) - 2).toInt()
    }
}
