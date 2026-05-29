package com.example.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable

@Immutable
data class UsbDeviceDetails(
    val name: String,
    val vendorId: String,
    val productId: String,
    val deviceClass: String,
    val manufacturer: String,
    val product: String,
    val maxSpeedMbps: Int,
    // Enhanced storage device details
    val brand: String = manufacturer,
    val speedClassString: String = "USB 2.0 Standard High-Speed",
    val usbVersion: String = "USB 2.0",
    val releaseYear: Int = 2015,
    val estimatedAgeYears: Int = 11,
    val storageCapacityGB: Int = 0,
    val fileSystem: String = "FAT32",
    val isStorage: Boolean = false
)

@Immutable
data class UsbStateInfo(
    val isConnected: Boolean = false,
    val powerSource: String = "Battery/Unplugged",
    val chargingCurrentAmperes: Double = 0.0,
    val chargingVoltageVolts: Double = 0.0,
    val chargingPowerWatts: Double = 0.0,
    val maxCurrentAmperes: Double = 0.0,
    val maxVoltageVolts: Double = 0.0,
    val maxPowerWatts: Double = 0.0,
    val batteryLevel: Int = 0,
    val batteryTemperatureCelsius: Double = 0.0,
    val isHostConnected: Boolean = false,
    val isConfigured: Boolean = false,
    val isUnlocked: Boolean = false,
    val usbDevices: List<UsbDeviceDetails> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class CableClassification(
    val name: String,
    val maxSpeedGbps: Double,
    val maxPowerWatts: Int,
    val isFullFeature: Boolean,
    val videoAltModeSupported: Boolean,
    val typicalWiringText: String,
    val physicalPinsRequired: String,
    val compatibilityRating: Double // 0.0 to 1.0 probability based on sensor inputs
)

@Immutable
data class DiagnosticResult(
    val isSuccess: Boolean = false,
    val estimatedResistanceOhms: Double = 0.0,
    val noiseLevelPct: Double = 0.0,
    val voltageStabilityPct: Double = 100.0,
    val temperatureDelta: Double = 0.0,
    val throughputMbpsEst: Double = 0.0,
    val classification: String = "Unknown Cable",
    val remarks: String = ""
)
