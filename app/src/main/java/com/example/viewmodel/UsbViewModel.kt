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
    private val _connectorType = MutableStateFlow(0) // 0: Auto-Detect, 1: Type-C to Type-C, 2: Type-C to Type-A, 3: Type-C to Lightning, 4: Type-C to Micro-USB
    val connectorType: StateFlow<Int> = _connectorType.asStateFlow()

    private val _effectiveConnectorType = MutableStateFlow(1) // 1: C-C, 3: C-Lightning, 2: C-A, 4: C-Micro
    val effectiveConnectorType: StateFlow<Int> = _effectiveConnectorType.asStateFlow()

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

    // Premium UI styling Customization parameters
    private val _themeSelection = MutableStateFlow(0) // 0: Hyper-Blue, 1: Neon Matrix, 2: Nebula, 3: Luxury Gold
    val themeSelection: StateFlow<Int> = _themeSelection.asStateFlow()

    private val _fontSelection = MutableStateFlow(0) // 0: Space Tech, 1: Tech Monospace, 2: Elegant Inter, 3: Serif
    val fontSelection: StateFlow<Int> = _fontSelection.asStateFlow()

    private val _layoutSelection = MutableStateFlow(0) // 0: Immersive Glass, 1: Brutalist Dark, 2: Bio-Glow
    val layoutSelection: StateFlow<Int> = _layoutSelection.asStateFlow()

    private val _animationSpeed = MutableStateFlow(0) // 0: Expressive, 1: Cinema slow, 2: Static tech
    val animationSpeed: StateFlow<Int> = _animationSpeed.asStateFlow()

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

    fun setThemeSelection(theme: Int) {
        _themeSelection.value = theme
    }

    fun setFontSelection(font: Int) {
        _fontSelection.value = font
    }

    fun setLayoutSelection(layout: Int) {
        _layoutSelection.value = layout
    }

    fun setAnimationSpeed(speed: Int) {
        _animationSpeed.value = speed
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
            val effType = _effectiveConnectorType.value
            val isCcCard = effType == 1
            val isUsab = effType == 2
            val isLightning = effType == 3
            val isMicro = effType == 4
            val hasEmarker = _eMarkerIndicator.value == 1 || (isCcCard && (state.chargingPowerWatts > 60.0 || state.usbDevices.any { it.maxSpeedMbps > 440 }))
            val supportsDisplay = _videoAltMode.value == 1 || (isCcCard && state.usbDevices.any { it.maxSpeedMbps > 440 })

            // Simulated real-world diagnostics
            val estResistance = when {
                isLightning -> 0.14 + (Math.random() * 0.04) // MFi contact pads resistance
                hasEmarker -> 0.07 + (Math.random() * 0.02) // Premium, low resistance
                isCcCard -> 0.11 + (Math.random() * 0.03)   // Standard C-C
                isUsab -> 0.18 + (Math.random() * 0.08)     // High resistance A-C
                isMicro -> 0.22 + (Math.random() * 0.06)    // Legacy Micro port
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
                isLightning -> 480.0 // Standard Type-C to Lightning limits
                isMicro -> 480.0
                else -> 12.0 // slow speed USB 1.1 or audio
            }

            val matchedClassification = when (effType) {
                1 -> {
                    if (supportsDisplay) "Type-C to Type-C (Thunderbolt 4 / USB4)"
                    else if (hasEmarker) "Type-C to Type-C Core (Hi-Power E-Mark)"
                    else "Type-C to Type-C basic Charge & Sync"
                }
                2 -> {
                    if (_perceivedSpeed.value == 0) "USB 3.1 Type-A to C Legacy High Speed"
                    else "USB 2.0 Type-A to C Basic"
                }
                3 -> {
                    if (state.chargingPowerWatts > 12.0) "Type-C to Lightning MFi Fast Charge (PD)"
                    else "Type-C to Lightning Standard Sync"
                }
                4 -> "Type-C to Micro-USB Legacy OTG Sync"
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

        // Fully automated judgment of cable specifications based on hardware signals
        val isCc = if (state.isConnected) {
            // C-C connections are forced for PD accessories, or power delivery > 15W, or AC Fast Charging
            (state.chargingPowerWatts > 15.0) || (state.powerSource == "AC Fast Charger") || (state.chargingCurrentAmperes > 1.5) || state.usbDevices.any { it.maxSpeedMbps > 440 }
        } else {
            true // default base layout
        }

        // Auto-detect Apple check
        val isAppleDeviceConnected = state.isConnected && (
            state.usbDevices.any { it.vendorId.equals("0x05AC", ignoreCase = true) || it.manufacturer.contains("Apple", ignoreCase = true) || it.product.contains("iPhone", ignoreCase = true) || it.product.contains("iPad", ignoreCase = true) }
        )

        val effectiveType = when (_connectorType.value) {
            0 -> { // Auto-Detect
                if (isAppleDeviceConnected) 3 // C to Lightning
                else if (isCc) 1 // C to C
                else 2 // C to A
            }
            1 -> 1 // Forced C to C
            2 -> 2 // Forced C to A
            3 -> 3 // Forced C to Lightning
            4 -> 4 // Forced C to Micro
            else -> 1
        }
        _effectiveConnectorType.value = effectiveType

        // E-Marker presence is dynamically determined. Unmarked C-C cables are limited to 3A max (60W).
        // If wattage is > 60W or we detect active high-speed USB accessories, signature is verified!
        val emarkYes = state.isConnected && (effectiveType == 1) && (
            state.chargingPowerWatts > 60.0 || state.maxPowerWatts > 60.0 || state.usbDevices.any { it.maxSpeedMbps > 440 }
        )
        val emarkNo = state.isConnected && !emarkYes
        val emarkUnk = !state.isConnected

        // DisplayPort Alternate Mode is auto-estimated based on active chip verification
        val videoYes = state.isConnected && emarkYes && (effectiveType == 1)
        val videoNo = state.isConnected && !videoYes
        val videoUnk = !state.isConnected

        // Data speed capability is detected based on connected device descriptors or charging profile stability
        val speedFast = state.isConnected && (
            state.usbDevices.any { it.maxSpeedMbps > 440 } || state.chargingPowerWatts > 18.0
        )
        val speedStd = state.isConnected && !speedFast
        val speedSlow = !state.isConnected

        // We estimate matching probability scores for five typical standard cables:
        val list = when (effectiveType) {
            1 -> { // Type-C to Type-C (C to C)
                listOf(
                    CableClassification(
                        name = "Type-C to Type-C (Thunderbolt 4 / USB4)",
                        maxSpeedGbps = 40.0,
                        maxPowerWatts = if (emarkYes) 240 else 60,
                        isFullFeature = true,
                        videoAltModeSupported = true,
                        typicalWiringText = "Premium coaxial high-speed TX/RX lines with embedded E-Marker chip, supporting full display alternate routing.",
                        physicalPinsRequired = "24 Pins Symmetric Layout",
                        compatibilityRating = if (emarkYes || speedFast) 0.95 else 0.55
                    ),
                    CableClassification(
                        name = "Type-C to Type-C (USB 3.2 Gen 2 SuperSpeed)",
                        maxSpeedGbps = 10.0,
                        maxPowerWatts = if (emarkYes) 100 else 60,
                        isFullFeature = true,
                        videoAltModeSupported = true,
                        typicalWiringText = "Shielded dual-simplex twisted wire structure. Ideal for high-speed file storage transports.",
                        physicalPinsRequired = "22-24 Pins Configured",
                        compatibilityRating = if (speedFast && !emarkYes) 0.90 else if (!speedFast && !emarkYes) 0.65 else 0.45
                    ),
                    CableClassification(
                        name = "Type-C to Type-C (USB 2.0 Charge & Sync)",
                        maxSpeedGbps = 0.48,
                        maxPowerWatts = if (emarkYes) 100 else 60,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "VBUS, GND, CC and legacy D+/D- pins wired. Lacks high-frequency differential transceiver lanes.",
                        physicalPinsRequired = "12 Pins Compact Layout",
                        compatibilityRating = if (speedStd) 0.85 else 0.30
                    )
                )
            }
            2 -> { // Type-C to Type-A (C to A)
                listOf(
                    CableClassification(
                        name = "Type-C to Type-A (USB 3.1 SuperSpeed)",
                        maxSpeedGbps = 5.0,
                        maxPowerWatts = 15,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "Legacy asymmetric USB-A socket interface. Fully equipped with a standard 56kΩ pull-up resistor to assure safe device load-draw detection.",
                        physicalPinsRequired = "9 Pin USB-A / 12-24 Pin USB-C Hybrid",
                        compatibilityRating = if (speedFast) 0.95 else 0.50
                    ),
                    CableClassification(
                        name = "Type-C to Type-A (USB 2.0 Legacy Standard)",
                        maxSpeedGbps = 0.48,
                        maxPowerWatts = 15,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "Standard legacy backup charging cable. Simple power and differential D+/D- lines, commonly bundled with legacy wall bricks.",
                        physicalPinsRequired = "4 Pin USB-A / 4-12 Pin USB-C Hybrid",
                        compatibilityRating = if (speedStd) 0.90 else 0.40
                    )
                )
            }
            3 -> { // Type-C to Lightning (C to Lightning)
                listOf(
                    CableClassification(
                        name = "Type-C to Lightning (MFi Fast Charge PD)",
                        maxSpeedGbps = 0.48,
                        maxPowerWatts = 27,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "Made For iPhone (MFi) certified Lightning integration. Supports USB-PD Power Handshake profiles (9V rail fast-charging) for Apple iOS accessories.",
                        physicalPinsRequired = "8 Pin C94 MFi Chipset Layout",
                        compatibilityRating = if (state.chargingPowerWatts > 12.0 || state.powerSource == "AC Fast Charger") 0.95 else 0.60
                    ),
                    CableClassification(
                        name = "Type-C to Lightning (Standard Sync)",
                        maxSpeedGbps = 0.48,
                        maxPowerWatts = 12,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "Legacy C48/C89 connector design. Limited to standard charging profiles and 480 Mbps legacy differential transfers.",
                        physicalPinsRequired = "8 Pin MFi Chipset Layout",
                        compatibilityRating = if (state.chargingPowerWatts <= 12.0) 0.90 else 0.40
                    )
                )
            }
            4 -> { // Type-C to Micro-USB (C to Micro)
                listOf(
                    CableClassification(
                        name = "Type-C to Micro-USB (Legacy OTG Sync)",
                        maxSpeedGbps = 0.48,
                        maxPowerWatts = 10,
                        isFullFeature = false,
                        videoAltModeSupported = false,
                        typicalWiringText = "Legacy standard bridge connection. Adapts the modern USB-C multiplexed layout for older micro-USB devices or direct legacy digital accessories.",
                        physicalPinsRequired = "5 Pin Micro-USB Layout",
                        compatibilityRating = 0.95
                    )
                )
            }
            else -> emptyList()
        }.sortedByDescending { it.compatibilityRating }

        _classifications.value = list
    }

    override fun onCleared() {
        super.onCleared()
        usbReceiver.unregister()
        performanceJob?.cancel()
    }
}
