package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CableClassification
import com.example.data.DiagnosticResult
import com.example.data.UsbDeviceDetails
import com.example.data.UsbStateInfo
import com.example.receiver.UsbReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class UsbViewModel(application: Application) : AndroidViewModel(application) {

    private val _usbState = MutableStateFlow(UsbStateInfo())
    val usbState: StateFlow<UsbStateInfo> = _usbState.asStateFlow()

    // Interactive profiler variables
    private val _connectorType = MutableStateFlow(0) // 0: USB-C to C, 1: USB-A to C, 2: Micro/Other
    val connectorType: StateFlow<Int> = _connectorType.asStateFlow()

    private val _eMarkerIndicator = MutableStateFlow(0) // 0: Unknown, 1: Yes (Logo/Label), 2: No
    val eMarkerIndicator: StateFlow<Int> = _eMarkerIndicator.asStateFlow()

    private val _videoAltMode = MutableStateFlow(0) // 0: Unknown, 1: Works (displays 4K video), 2: Fails
    val videoAltMode: StateFlow<Int> = _videoAltMode.asStateFlow()

    private val _perceivedSpeed = MutableStateFlow(0) // 0: Fast (SuperSpeed+), 1: Standard (USB 2.0), 2: Raw slow
    val perceivedSpeed: StateFlow<Int> = _perceivedSpeed.asStateFlow()

    // Calculated classifications based on hardware + manual answers
    private val _classifications = MutableStateFlow<List<CableClassification>>(emptyList())
    val classifications: StateFlow<List<CableClassification>> = _classifications.asStateFlow()

    // Diagnostic Simulation State
    private val _diagnosticState = MutableStateFlow<DiagnosticState>(DiagnosticState.Idle)
    val diagnosticState: StateFlow<DiagnosticState> = _diagnosticState.asStateFlow()

    // Active diagnostic result
    private val _diagnosticResult = MutableStateFlow<DiagnosticResult?>(null)
    val diagnosticResult: StateFlow<DiagnosticResult?> = _diagnosticResult.asStateFlow()

    // Historical Performance ticks
    private val _powerHistory = MutableStateFlow<List<HistoryTick>>(emptyList())
    val powerHistory: StateFlow<List<HistoryTick>> = _powerHistory.asStateFlow()

    private var performanceJob: Job? = null
    private val maxHistoryPoints = 30
    private var tickCount = 0

    private val usbReceiver = UsbReceiver(application) { info ->
        _usbState.value = info
        recalculateCableMatch()
        recordTick(info)
    }

    sealed interface DiagnosticState {
        object Idle : DiagnosticState
        data class Running(val step: String, val progress: Float) : DiagnosticState
        object Finished : DiagnosticState
    }

    data class HistoryTick(
        val label: String,
        val powerWatts: Double,
        val currentAmps: Double,
        val voltageVolts: Double
    )

    init {
        usbReceiver.register()
        startHistoricalLogger()
        recalculateCableMatch()
    }

    fun setConnectorType(type: Int) {
        _connectorType.value = type
        recalculateCableMatch()
    }

    fun setEMarkerIndicator(indicator: Int) {
        _eMarkerIndicator.value = indicator
        recalculateCableMatch()
    }

    fun setVideoAltMode(mode: Int) {
        _videoAltMode.value = mode
        recalculateCableMatch()
    }

    fun setPerceivedSpeed(speed: Int) {
        _perceivedSpeed.value = speed
        recalculateCableMatch()
    }

    private fun startHistoricalLogger() {
        performanceJob?.cancel()
        performanceJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                recordTick(_usbState.value)
            }
        }
    }

    private fun recordTick(info: UsbStateInfo) {
        val nextTick = HistoryTick(
            label = "${tickCount++}s",
            powerWatts = if (info.isConnected) info.chargingPowerWatts else 0.0,
            currentAmps = if (info.isConnected) info.chargingCurrentAmperes else 0.0,
            voltageVolts = if (info.isConnected) info.chargingVoltageVolts else 0.0
        )
        _powerHistory.update { currentList ->
            val updated = currentList.toMutableList()
            updated.add(nextTick)
            if (updated.size > maxHistoryPoints) {
                updated.removeAt(0)
            }
            updated
        }
    }

    fun runDiagnosticSweep() {
        viewModelScope.launch {
            _diagnosticState.value = DiagnosticState.Running("Initializing signal probes...", 0.1f)
            delay(800)
            _diagnosticState.value = DiagnosticState.Running("Sensing impedance differential...", 0.35f)
            delay(1000)
            _diagnosticState.value = DiagnosticState.Running("Analyzing PD Power Rules handshakes...", 0.65f)
            delay(900)
            _diagnosticState.value = DiagnosticState.Running("Validating Alternate Mode wiring pinout...", 0.85f)
            delay(800)

            // Calculate mock parameters modeled from state + wizard input
            val state = _usbState.value
            val isCcCard = _connectorType.value == 0
            val hasEmarker = _eMarkerIndicator.value == 1
            val supportsDisplay = _videoAltMode.value == 1
            val isUsab = _connectorType.value == 1

            // Simulated real-world diagnostics
            val estResistance = when {
                hasEmarker -> 0.07 + (Math.random() * 0.02) // Premium, low resistance
                isCcCard -> 0.11 + (Math.random() * 0.03)   // Standard C-C
                isUsab -> 0.18 + (Math.random() * 0.08)     // High resistance A-C
                else -> 0.25 + (Math.random() * 0.15)
            }

            val estStability = 100.0 - (estResistance * 45.0) - (Math.random() * 3.0)
            val noiseLevel = (estResistance * 20.0) + (Math.random() * 2.0)

            val maxSpeedEst = when {
                supportsDisplay -> 40000.0 // 40 Gbps
                hasEmarker && _perceivedSpeed.value == 0 -> 20000.0 // 20 Gbps / USB 3.2 Gen 2x2
                _perceivedSpeed.value == 0 -> 10000.0 // 10 Gbps
                isCcCard && _perceivedSpeed.value == 1 -> 480.0
                isUsab && _perceivedSpeed.value == 1 -> 480.0
                else -> 12.0 // slow speed USB 1.1 or audio
            }

            val matchedClassification = when {
                supportsDisplay -> "USB4 / Thunderbolt 4 (Active E-Mark)"
                hasEmarker && isCcCard -> "USB 3.2 Gen 2 Type-C (Hi-Power E-Mark)"
                isCcCard && _perceivedSpeed.value == 0 -> "USB 3.2 Gen 1 Type-C (SuperSpeed 5Gbps)"
                isCcCard -> "USB 2.0 Type-C to C (Charge & Sync)"
                isUsab && _perceivedSpeed.value == 0 -> "USB 3.1 Type-A to C (SuperSpeed)"
                isUsab -> "USB 2.0 Type-A to C Basic"
                else -> "Standard Charging Adapter Connection"
            }

            val powerLabel = if (state.isConnected) "${String.format("%.2f", state.chargingPowerWatts)}W" else "unplugged"
            val remarksText = when {
                supportsDisplay -> "Excellent signal integrity! Active e-Mark chips detected. Ideal for Thunderbolt 4 displays & high-performance storage."
                hasEmarker -> "E-Marker present. Safely rated for up to 100W/240W charging. High-quality power delivery wiring."
                estResistance > 0.2 -> "High contact resistance detected (>${String.format("%.2f", estResistance)}Ω). Charging may be throttled to prevent overheating."
                else -> "Standard cable verified. Stable impedance. Good backup or general charging cable."
            }

            _diagnosticResult.value = DiagnosticResult(
                isSuccess = true,
                estimatedResistanceOhms = estResistance,
                noiseLevelPct = noiseLevel,
                voltageStabilityPct = estStability.coerceIn(50.0, 100.0),
                temperatureDelta = if (state.isConnected) (state.chargingPowerWatts * 0.18) else 0.0,
                throughputMbpsEst = maxSpeedEst,
                classification = matchedClassification,
                remarks = remarksText
            )
            _diagnosticState.value = DiagnosticState.Finished
        }
    }

    fun resetDiagnostics() {
        _diagnosticState.value = DiagnosticState.Idle
        _diagnosticResult.value = null
    }

    // Simulation capabilities for USB Storage Drives / Pendrives
    fun simulateStorageDevice(type: Int) {
        val simulated = when (type) {
            1 -> UsbDeviceDetails(
                name = "/dev/bus/usb/001/012",
                vendorId = "0x0951",
                productId = "0x1666",
                deviceClass = "Mass Storage (Disk)",
                manufacturer = "Kingston Technology Ltd.",
                product = "DataTraveler Exodia USB Flash Duo",
                maxSpeedMbps = 5000,
                brand = "Kingston",
                speedClassString = "USB 3.2 Gen 1 (SuperSpeed 5Gbps)",
                usbVersion = "USB 3.2 Gen 1",
                releaseYear = 2021,
                estimatedAgeYears = 5,
                storageCapacityGB = 128,
                fileSystem = "FAT32",
                isStorage = true
            )
            2 -> UsbDeviceDetails(
                name = "/dev/bus/usb/001/014",
                vendorId = "0x0781",
                productId = "0x5581",
                deviceClass = "Mass Storage (Disk)",
                manufacturer = "SanDisk Corporation",
                product = "Cruzer Glide OTG PenDrive",
                maxSpeedMbps = 480,
                brand = "SanDisk",
                speedClassString = "USB 2.0 High-Speed (480Mbps)",
                usbVersion = "USB 2.0",
                releaseYear = 2012,
                estimatedAgeYears = 14,
                storageCapacityGB = 16,
                fileSystem = "FAT32",
                isStorage = true
            )
            3 -> UsbDeviceDetails(
                name = "/dev/bus/usb/001/018",
                vendorId = "0x04E8",
                productId = "0x61F5",
                deviceClass = "Mass Storage (Disk)",
                manufacturer = "Samsung Electronics Co., Ltd.",
                product = "Portable SSD T7 Shield Metallic",
                maxSpeedMbps = 10000,
                brand = "Samsung",
                speedClassString = "USB 3.2 Gen 2 (Extreme 10Gbps NVMe Hub)",
                usbVersion = "USB 3.2 Gen 2x2",
                releaseYear = 2024,
                estimatedAgeYears = 2,
                storageCapacityGB = 1024,
                fileSystem = "exFAT",
                isStorage = true
            )
            else -> null
        }

        _usbState.update { current ->
            val updatedList = current.usbDevices.toMutableList()
            updatedList.removeAll { it.isStorage }
            if (simulated != null) {
                updatedList.add(simulated)
            }
            current.copy(
                isConnected = true,
                isHostConnected = true,
                isConfigured = true,
                usbDevices = updatedList
            )
        }
        recalculateCableMatch()
    }

    fun clearSimulatedDevices() {
        _usbState.update { current ->
            current.copy(
                usbDevices = current.usbDevices.filter { !it.isStorage }
            )
        }
        recalculateCableMatch()
    }

    private fun recalculateCableMatch() {
        val state = _usbState.value
        val isCc = _connectorType.value == 0
        val isAc = _connectorType.value == 1
        val isOther = _connectorType.value == 2

        val emarkYes = _eMarkerIndicator.value == 1
        val emarkNo = _eMarkerIndicator.value == 2
        val emarkUnk = _eMarkerIndicator.value == 0

        val videoYes = _videoAltMode.value == 1
        val videoNo = _videoAltMode.value == 2
        val videoUnk = _videoAltMode.value == 0

        val speedFast = _perceivedSpeed.value == 0
        val speedStd = _perceivedSpeed.value == 1
        val speedSlow = _perceivedSpeed.value == 2

        // We estimate matching probability scores for five typical standard cables:
        val list = listOf(
            // 1. USB4 Gen 3 Type-C to C (40 Gbps, 100/240W, DP Alt Mode, E-Mark)
            run {
                var p = 0.0
                if (isCc) p += 0.4
                if (emarkYes) p += 0.2
                if (videoYes) p += 0.3
                if (speedFast) p += 0.1
                if (isAc || isOther) p = 0.0
                CableClassification(
                    name = "USB4 Gen 3 / Thunderbolt 4 (High-Speed Active)",
                    maxSpeedGbps = 40.0,
                    maxPowerWatts = if (emarkYes) 240 else 60,
                    isFullFeature = true,
                    videoAltModeSupported = true,
                    typicalWiringText = "Fully Shielded Coaxial Cores with Active E-Marker microcontroller.",
                    physicalPinsRequired = "24 Pins Complete Layout",
                    compatibilityRating = p.coerceIn(0.01, 0.99)
                )
            },
            // 2. USB 3.2 Gen 2 Type-C (10 Gbps, DP-Alt compatible, standard power 60W or 100W with emark)
            run {
                var p = 0.0
                if (isCc) p += 0.4
                if (emarkUnk) p += 0.1 else if (emarkNo) p += 0.1
                if (videoYes) p += 0.3
                if (speedFast) p += 0.2
                if (isAc || isOther) p = 0.0
                CableClassification(
                    name = "USB 3.2 Gen 2 Type-C (SuperSpeed+ 10Gbps)",
                    maxSpeedGbps = 10.0,
                    maxPowerWatts = if (emarkYes) 100 else 60,
                    isFullFeature = true,
                    videoAltModeSupported = true,
                    typicalWiringText = "Stitched signal lanes, dual shielded twisted pairs.",
                    physicalPinsRequired = "22-24 Pins Configured",
                    compatibilityRating = p.coerceIn(0.01, 0.99)
                )
            },
            // 3. USB 2.0 Type-C to Type-C (480 Mbps, basic charge & sync, no video support)
            run {
                var p = 0.0
                if (isCc) p += 0.4
                if (emarkNo) p += 0.2 else if (emarkUnk) p += 0.1
                if (videoNo) p += 0.2
                if (speedStd) p += 0.2
                if (isAc || isOther) p = 0.0
                CableClassification(
                    name = "USB 2.0 Type-C to Type-C Core",
                    maxSpeedGbps = 0.48,
                    maxPowerWatts = if (emarkYes) 100 else 60,
                    isFullFeature = false,
                    videoAltModeSupported = false,
                    typicalWiringText = "VBUS, GND, CC1, CC2, and D+/D- pins wired. No high-speed Tx/Rx matrices.",
                    physicalPinsRequired = "12 Pins Compact Layout",
                    compatibilityRating = p.coerceIn(0.01, 0.99)
                )
            },
            // 4. USB 3.1 Type-A to Type-C (5/10 Gbps SuperSpeed, charging locked to standard USB profiles)
            run {
                var p = 0.0
                if (isAc) p += 0.5
                if (speedFast) p += 0.3
                if (videoNo) p += 0.2
                if (isCc || isOther) p = 0.0
                val customMaxPowerWatts = if (state.maxPowerWatts > 0.0) state.maxPowerWatts.toInt() else 15
                CableClassification(
                    name = "USB 3.1 Type-A to Type-C Legacy High Speed",
                    maxSpeedGbps = 5.0,
                    maxPowerWatts = customMaxPowerWatts,
                    isFullFeature = false,
                    videoAltModeSupported = false,
                    typicalWiringText = "Legacy compatibility pull-up resistor (56kΩ) wired to CC lines.",
                    physicalPinsRequired = "9 Pins Legacy Blue Core",
                    compatibilityRating = p.coerceIn(0.01, 0.99)
                )
            },
            // 5. USB 2.0 Type-A to Type-C Basic
            run {
                var p = 0.0
                if (isAc) p += 0.5
                if (speedStd) p += 0.3
                if (videoNo) p += 0.2
                if (isCc || isOther) p = 0.0
                val customMaxPowerWatts = if (state.maxPowerWatts > 0.0) state.maxPowerWatts.toInt() else 15
                CableClassification(
                    name = "USB 2.0 Type-A to Type-C Basic Utility",
                    maxSpeedGbps = 0.48,
                    maxPowerWatts = customMaxPowerWatts,
                    isFullFeature = false,
                    videoAltModeSupported = false,
                    typicalWiringText = "Standard charge & basic data lines. Minimal shielding, standard resistance.",
                    physicalPinsRequired = "4-5 Pins (Power + Legacy Data)",
                    compatibilityRating = p.coerceIn(0.01, 0.99)
                )
            }
        ).sortedByDescending { it.compatibilityRating }

        _classifications.value = list
    }

    override fun onCleared() {
        super.onCleared()
        usbReceiver.unregister()
        performanceJob?.cancel()
    }
}
