package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.DiscoveredDevice
import com.syntx34.wifilab.domain.model.RecommendationSeverity
import com.syntx34.wifilab.domain.model.SecurityRecommendation

object AuditRecommendations {

    fun build(
        devices: List<DiscoveredDevice>,
        unexpectedDevices: List<DiscoveredDevice>,
        baselineUsed: Boolean,
        gatewayIpv4: String?,
    ): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()

        if (unexpectedDevices.isNotEmpty()) {
            recommendations += SecurityRecommendation(
                title = "Review ${unexpectedDevices.size} unrecognized device${if (unexpectedDevices.size == 1) "" else "s"}",
                detail = "Compare the new addresses with devices you expect. Confirm the device before changing router access.",
                severity = RecommendationSeverity.HIGH,
            )
        }

        if (!baselineUsed) {
            recommendations += SecurityRecommendation(
                title = "Save a trusted baseline",
                detail = "After verifying every device, save the current result as a baseline so future audits can identify additions.",
                severity = RecommendationSeverity.INFO,
            )
        }

        val webAdminDevices = devices.filter { device ->
            device.openPorts.any { it == 80 || it == 443 || it == 8080 || it == 8443 }
        }
        if (webAdminDevices.isNotEmpty()) {
            recommendations += SecurityRecommendation(
                title = "Protect local administration interfaces",
                detail = "A web interface is reachable on ${webAdminDevices.size} device${if (webAdminDevices.size == 1) "" else "s"}. Use a unique administrator password and current firmware.",
                severity = RecommendationSeverity.MEDIUM,
            )
        }

        val fileSharing = devices.filter { 445 in it.openPorts }
        if (fileSharing.isNotEmpty()) {
            recommendations += SecurityRecommendation(
                title = "Limit local file sharing",
                detail = "SMB is reachable on ${fileSharing.size} device${if (fileSharing.size == 1) "" else "s"}. Disable sharing that is not needed and avoid guest network exposure.",
                severity = RecommendationSeverity.HIGH,
            )
        }

        val remoteShell = devices.filter { 22 in it.openPorts }
        if (remoteShell.isNotEmpty()) {
            recommendations += SecurityRecommendation(
                title = "Review remote shell access",
                detail = "SSH is reachable on ${remoteShell.size} device${if (remoteShell.size == 1) "" else "s"}. Use key based authentication and restrict which hosts may connect.",
                severity = RecommendationSeverity.MEDIUM,
            )
        }

        val exposedCameras = devices.filter { 554 in it.openPorts }
        if (exposedCameras.isNotEmpty()) {
            recommendations += SecurityRecommendation(
                title = "Check camera exposure",
                detail = "RTSP is reachable on ${exposedCameras.size} device${if (exposedCameras.size == 1) "" else "s"}. Put cameras on an isolated network and change default credentials.",
                severity = RecommendationSeverity.MEDIUM,
            )
        }

        if (gatewayIpv4 != null) {
            recommendations += SecurityRecommendation(
                title = "Keep router firmware current",
                detail = "The gateway at $gatewayIpv4 should have automatic security updates enabled and remote administration disabled.",
                severity = RecommendationSeverity.LOW,
            )
        }

        recommendations += SecurityRecommendation(
            title = "Keep audit data local",
            detail = "WiFiLab stores findings on this device only. Export or share results deliberately, not automatically.",
            severity = RecommendationSeverity.INFO,
        )

        return recommendations
    }
}
