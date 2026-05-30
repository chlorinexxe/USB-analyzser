package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.BatteryManager
import android.util.Log
import com.example.data.UsbDeviceDetails
import com.example.data.UsbStateInfo

class UsbReceiver(
    private val context: Context,
    private val onUpdate: (UsbStateInfo) -> Unit
) {
    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            updateInfo(intent)
        }
    }

    fun register() {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            // Register standard implicit android hardware action
            addAction("android.hardware.usb.action.USB_STATE")
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                    isRegistered = true
                } catch (e: SecurityException) {
                    Log.w("UsbReceiver", "Failed to register with RECEIVER_EXPORTED, trying RECEIVER_NOT_EXPORTED", e)
                    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                    isRegistered = true
                }
            } else {
                context.registerReceiver(receiver, filter)
                isRegistered = true
            }
        } catch (e: Exception) {
            Log.e("UsbReceiver", "Failed to register broadcast receiver entirely", e)
        }

        // Initial fetch
        val initialIntent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (e: Exception) {
            Log.e("UsbReceiver", "Failed to fetch initial battery sticky broadcast", e)
            null
        }
        if (initialIntent != null) {
            updateInfo(initialIntent)
        }
    }

    fun unregister() {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        isRegistered = false
    }

    private fun updateInfo(intent: Intent) {
        try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

            var isConnected = false
            var powerSource = "Battery/Unplugged"
            var batteryLevel = 0
            var temperature = 0.0
            var voltageVolts = 0.0

            // Get battery changed extras
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                batteryLevel = (level * 100) / scale
            }

            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            if (temp >= 0) {
                temperature = temp / 10.0
            }

            val volt = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            if (volt >= 0) {
                // Volt can be either millivolts or volts depending on device, standard is millivolts
                voltageVolts = if (volt > 100) volt / 1000.0 else volt.toDouble()
            }

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                isConnected = true
                powerSource = "USB Port"
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
                isConnected = true
                powerSource = "AC Fast Charger"
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                isConnected = true
                powerSource = "Wireless Pad"
            }

            // Check if charging via any wired source
            val isWired = plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_AC

            // Max Charging parameters (microamperes & microvolts)
            // Available since Android 5.0 (Lollipop) from some battery implementations
            var maxCurrentAmps = 0.0
            var maxVoltageV = 0.0

            val maxCurrentMicroAmps = intent.getIntExtra("max_charging_current", -1)
            val maxVoltageMicroVolts = intent.getIntExtra("max_charging_voltage", -1)

            if (maxCurrentMicroAmps > 0) {
                maxCurrentAmps = maxCurrentMicroAmps / 1000000.0
            }
            if (maxVoltageMicroVolts > 0) {
                maxVoltageV = maxVoltageMicroVolts / 1000000.0
            }

            // Dynamic live current draw (microamps)
            var currentAmps = 0.0
            if (batteryManager != null) {
                val microAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (microAmps != Int.MIN_VALUE) {
                    // Usually negative when discharging, positive when charging. Normalise
                    currentAmps = Math.abs(microAmps) / 1000000.0
                    if (plugged == -1 || plugged == 0) {
                        currentAmps = -currentAmps // discharging
                    }
                }
            }

            // Fallback for charging current if currentNow is unavailable
            if (isWired && currentAmps == 0.0) {
                currentAmps = if (maxCurrentAmps > 0) maxCurrentAmps * 0.7 else 0.5 // estimation
            }

            // Fallback for charging voltage
            var chargingVoltageVal = voltageVolts
            if (isWired && maxVoltageV > 0.0) {
                chargingVoltageVal = maxVoltageV
            }

            val calculatedPowerWatts = if (isWired) Math.abs(currentAmps) * voltageVolts else 0.0
            val calculatedMaxPowerWatts = maxCurrentAmps * maxVoltageV

            // Check details from USB_STATE action if triggered
            var isHostConnected = false
            var isConfigured = false
            var isUnlocked = false

            if (intent.action == "android.hardware.usb.action.USB_STATE" || intent.action == Intent.ACTION_BATTERY_CHANGED) {
                // If it is the sticky broadcast, fetch the state
                val stateIntent = if (intent.action == "android.hardware.usb.action.USB_STATE") intent
                else {
                    try {
                        context.registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
                    } catch (_: Exception) { null }
                }

                if (stateIntent != null) {
                    isConnected = isConnected || stateIntent.getBooleanExtra("connected", false)
                    isHostConnected = stateIntent.getBooleanExtra("host_connected", false)
                    isConfigured = stateIntent.getBooleanExtra("configured", false)
                    isUnlocked = stateIntent.getBooleanExtra("unlocked", false)
                }
            }

            // Fetch attached devices details (OTG / USB accessories)
            val usbDevicesList = mutableListOf<UsbDeviceDetails>()
            try {
                val devicesMap = usbManager.deviceList
                devicesMap.values.forEach { dev ->
                    val name = dev.deviceName
                    val vId = String.format("0x%04X", dev.vendorId)
                    val pId = String.format("0x%04X", dev.productId)
                    val dClassVal = when (dev.deviceClass) {
                        0x00 -> "Interface Specific"
                        0x01 -> "Audio (DAC)"
                        0x02 -> "Communications"
                        0x03 -> "Human Interface (HID)"
                        0x08 -> "Mass Storage (Disk)"
                        0x09 -> "USB Hub"
                        0xFF -> "Vendor Specific"
                        else -> "Standard Class (${dev.deviceClass})"
                    }

                    // On newer systems, we can fetch descriptive tags
                    val manufacturer = dev.manufacturerName ?: "Unknown Mfr"
                    val product = dev.productName ?: "Unknown USB Product"

                    // Estimated speed based on device class and typical specifications
                    val maxSpeed = when (dev.deviceClass) {
                        0x08 -> 480 // Flash drives usually support High-Speed 480Mbps or SuperSpeed 5Gbps
                        0x01 -> 12  // Audio DACs typically use Full-Speed 12Mbps
                        0x03 -> 12  // Keyboard/mouses are 1.5 - 12 Mbps
                        else -> 480
                    }

                    usbDevicesList.add(
                        UsbDeviceDetails(
                            name = name,
                            vendorId = vId,
                            productId = pId,
                            deviceClass = dClassVal,
                            manufacturer = manufacturer,
                            product = product,
                            maxSpeedMbps = maxSpeed
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("UsbReceiver", "Error reading usb device list", e)
            }

            val pm = context.packageManager
            val hostFeat = pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_USB_HOST)
            val accFeat = pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_USB_ACCESSORY)

            onUpdate(
                UsbStateInfo(
                    isConnected = isConnected,
                    powerSource = powerSource,
                    chargingCurrentAmperes = Math.abs(currentAmps),
                    chargingVoltageVolts = chargingVoltageVal,
                    chargingPowerWatts = calculatedPowerWatts,
                    maxCurrentAmperes = maxCurrentAmps,
                    maxVoltageVolts = maxVoltageV,
                    maxPowerWatts = calculatedMaxPowerWatts,
                    batteryLevel = batteryLevel,
                    batteryTemperatureCelsius = temperature,
                    isHostConnected = isHostConnected,
                    isConfigured = isConfigured,
                    isUnlocked = isUnlocked,
                    usbDevices = usbDevicesList,
                    hasUsbHostFeature = hostFeat,
                    hasUsbAccessoryFeature = accFeat,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e("UsbReceiver", "Broadcast error", e)
        }
    }
}
