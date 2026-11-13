package com.syntx34.wifilab.core.util

import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeFormatter {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val decimal = DecimalFormat("#,##0.#")

    fun formatTimestamp(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Not available"
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter)
    }

    fun formatShortTimestamp(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Never"
        val zoned = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return "${zoned.format(shortDateFormatter)}, ${zoned.format(timeFormatter)}"
    }

    fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        if (epochMillis <= 0L) return "Never"
        val delta = (now - epochMillis).coerceAtLeast(0L)
        val minutes = delta / 60_000L
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 1_440 -> "${minutes / 60} hr ago"
            minutes < 10_080 -> "${minutes / 1_440} days ago"
            else -> formatShortTimestamp(epochMillis)
        }
    }

    fun humanDuration(seconds: Double): String {
        if (seconds.isNaN()) return "Unknown"
        if (seconds <= 0.0) return "Instant"
        if (seconds == Double.POSITIVE_INFINITY) return "Effectively forever"
        if (seconds < 1.0) return "< 1 second"
        val units = listOf(
            "second" to 1.0,
            "minute" to 60.0,
            "hour" to 3_600.0,
            "day" to 86_400.0,
            "year" to 31_557_600.0,
        )
        var chosen = units.first()
        for (unit in units) {
            if (seconds >= unit.second) chosen = unit
        }
        val value = seconds / chosen.second
        val rounded = decimal.format(value)
        val suffix = if (rounded == "1") "" else "s"
        val result = "$rounded ${chosen.first}$suffix"
        return if (seconds > 31_557_600.0 * 1_000_000) {
            "${decimal.format(seconds / 31_557_600.0)} years"
        } else {
            result
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1_024) return "$bytes B"
        val units = listOf("KB", "MB", "GB")
        var value = bytes.toDouble() / 1_024.0
        var unitIndex = 0
        while (value >= 1_024 && unitIndex < units.lastIndex) {
            value /= 1_024.0
            unitIndex++
        }
        return "${decimal.format(value)} ${units[unitIndex]}"
    }

    fun formatDurationMs(millis: Long): String {
        if (millis <= 0L) return "0 s"
        val totalSeconds = millis / 1_000.0
        return humanDuration(totalSeconds)
    }

    fun formatNumber(value: Long): String = decimal.format(value)
}
