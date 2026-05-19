package com.archeGlobal.one.utils

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    private val PUBLIC_IP_SERVICES = listOf(
        "https://api.ipify.org?format=text",
        "https://icanhazip.com",
        "https://ifconfig.me/ip",
        "https://checkip.amazonaws.com",
        "https://ipinfo.io/ip",
    )

    /**
     * Returns the first reachable public IP from the fallback list, or null if every
     * service is unreachable. Logs each attempt.
     */
    fun getDevicePublicIp(): String? {
        for (url in PUBLIC_IP_SERVICES) {
            val ip = fetchIp(url)
            if (!ip.isNullOrBlank()) {
                Log.d(TAG, "Device public IP: $ip (from $url)")
                return ip
            }
        }
        Log.w(TAG, "All public-IP services failed")
        return null
    }

    /**
     * Tries every service in [PUBLIC_IP_SERVICES] and returns true if ANY of them
     * reports a public IP equal to any entry in [officeIps]. This is the gate used
     * for punch-in / punch-out: the device is considered "on the office network" if
     * any provider confirms an outgoing IP that matches one of the configured
     * office IPs.
     *
     * Returns:
     *  - true  → matched at least one service against at least one office IP
     *  - false → reached at least one service but nothing matched
     *  - null  → could not reach any service (network down). Caller should treat
     *            this as a hard failure and prompt the user, not silently allow.
     */
    fun matchesOfficeIp(officeIps: List<String>): Boolean? {
        val targets = officeIps.map { it.trim() }.filter { it.isNotEmpty() }
        if (targets.isEmpty()) return false

        var anyServiceReached = false

        for (url in PUBLIC_IP_SERVICES) {
            val ip = fetchIp(url)
            if (ip == null) {
                Log.w(TAG, "matchesOfficeIp: $url unreachable")
                continue
            }
            anyServiceReached = true
            Log.d(TAG, "matchesOfficeIp: $url → '$ip' (targets=$targets)")
            if (targets.any { it.equals(ip, ignoreCase = true) }) {
                Log.d(TAG, "matchesOfficeIp: MATCH via $url (ip='$ip')")
                return true
            }
        }
        return if (anyServiceReached) {
            Log.d(TAG, "matchesOfficeIp: no service matched office IPs")
            false
        } else {
            Log.w(TAG, "matchesOfficeIp: no service was reachable")
            null
        }
    }

    private fun fetchIp(url: String): String? {
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                // ipinfo.io returns HTML for some user-agents; force plain
                setRequestProperty("Accept", "text/plain")
            }
            conn.inputStream.bufferedReader().use { reader ->
                reader.readText().trim().takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchIp $url failed: ${e.message}")
            null
        }
    }
}
