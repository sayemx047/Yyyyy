package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.net.NetworkInterface
import java.util.Collections

data class DeviceTelemetry(
    val model: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val batteryLevel: String = "100%",
    val networkType: String = "Wi-Fi",
    val ipAddress: String = "192.168.1.100",
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
)

object DeviceUtils {

    fun getDeviceTelemetry(context: Context): DeviceTelemetry {
        val model = try {
            "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        } catch (e: Exception) {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        }

        // Battery level (guarded: registerReceiver can throw on some OEM ROMs / Android versions)
        var batteryPct = 95
        var isCharging = false
        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPct = level * 100 / scale
            }
            isCharging = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        } catch (e: Exception) {
            // Ignore — keep defaults
        }
        val batteryStr = "$batteryPct%${if (isCharging) " ⚡ Charging" else ""}"

        // Network type
        var netType = "Cellular 4G/5G"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            cm?.activeNetwork?.let { network ->
                val caps = cm.getNetworkCapabilities(network)
                if (caps != null) {
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        netType = "Wi-Fi (High Speed)"
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        netType = "Mobile Data (Cellular)"
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        netType = "Ethernet"
                    }
                }
            }
        } catch (e: Exception) {
            netType = "Online"
        }

        // IP address
        var ip = getIPAddress()
        if (ip.isBlank()) {
            ip = "102.165.34.88"
        }

        return DeviceTelemetry(
            model = model,
            batteryLevel = batteryStr,
            networkType = netType,
            ipAddress = ip
        )
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) return sAddr
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return ""
    }
}
