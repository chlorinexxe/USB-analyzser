package com.example.ui

import android.hardware.usb.UsbDevice
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CableClassification
import com.example.data.DiagnosticResult
import com.example.data.UsbDeviceDetails
import com.example.data.UsbStateInfo
import com.example.viewmodel.UsbViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Futuristic Colors styled to fit Frosted Glass theme
val CyberTeal = Color(0xFF60A5FA) // blue-400
val CyberPink = Color(0xFFF472B6) // pink-400
val CyberPurple = Color(0xFF818CF8) // indigo-400
val DeepSpace = Color(0xFF0F1115) // Slate Dark
val FrostedWhite = Color(0x0DFFFFFF) // white/5
val FrostedStroke = Color(0x1AFFFFFF) // white/10
val DarkGlass = Color(0x0DFFFFFF) // white/5

@Composable
fun CableAnalyzerScreen(viewModel: UsbViewModel) {
    val usbState by viewModel.usbState.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val diagnosticState by viewModel.diagnosticState.collectAsState()
    val diagnosticResult by viewModel.diagnosticResult.collectAsState()
    val powerHistory by viewModel.powerHistory.collectAsState()

    val connectorType by viewModel.connectorType.collectAsState()
    val eMarkerIndicator by viewModel.eMarkerIndicator.collectAsState()
    val videoAltMode by viewModel.videoAltMode.collectAsState()
    val perceivedSpeed by viewModel.perceivedSpeed.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Live Dashboard, 1: Profiler, 2: Diagnostics, 3: Specs/Pinout

    // Background floating animated dots animation for depth/shimmer glassmorphism effect
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundParticles")
    val dotOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val dotOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background radial cyber-glow gradients
                drawRect(color = DeepSpace)
                // Blue-600 Glow top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x4D2563EB), Color.Transparent),
                        center = Offset(size.width * -0.1f + dotOffset1 * 0.3f, size.height * -0.1f),
                        radius = size.width * 1.1f
                    )
                )
                // Indigo-700 Glow bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x334338CA), Color.Transparent),
                        center = Offset(size.width * 1.1f + dotOffset2 * 0.3f, size.height * 1.1f),
                        radius = size.width * 1.0f
                    )
                )
                // Cyan Glow Center-Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A06B6D4), Color.Transparent),
                        center = Offset(size.width * 1.1f, size.height * 0.4f),
                        radius = size.width * 0.8f
                    )
                )
            },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ANALYZER v2.4",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal,
                            letterSpacing = 1.8.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (usbState.isConnected) CyberTeal else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CablePulse",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                    Text(
                        text = if (usbState.isConnected) "Active Connection Detected" else "Awaiting USB Interface Connection",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (usbState.isConnected) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.45f)
                        )
                    )
                }

                // Glass Connection Badge
                GlassPill(
                    text = if (usbState.isConnected) "PLUGGED" else "DISCONNECTED",
                    color = if (usbState.isConnected) CyberTeal else Color.LightGray.copy(alpha = 0.6f)
                )
            }

            // Tabs Bar selector
            GlassTabBar(
                tabs = listOf("DASHBOARD", "PROFILER", "DIAGNOSTICS", "PINOUT MAP"),
                selectedIndex = activeTab,
                onTabSelected = { activeTab = it }
            )

            // Content Area dependent on active tab selection
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = "ActiveTabContent"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> DashboardTab(usbState, viewModel)
                    1 -> ProfilerTab(
                        usbState = usbState,
                        connectorType = connectorType,
                        eMarkerIndicator = eMarkerIndicator,
                        videoAltMode = videoAltMode,
                        perceivedSpeed = perceivedSpeed,
                        classifications = classifications,
                        onSetConnector = { viewModel.setConnectorType(it) },
                        onSetEmarker = { viewModel.setEMarkerIndicator(it) },
                        onSetVideoAlt = { viewModel.setVideoAltMode(it) },
                        onSetSpeed = { viewModel.setPerceivedSpeed(it) }
                    )
                    2 -> DiagnosticsTab(
                        usbState = usbState,
                        diagnosticState = diagnosticState,
                        diagnosticResult = diagnosticResult,
                        powerHistory = powerHistory,
                        onStartSweep = { viewModel.runDiagnosticSweep() },
                        onResetSweep = { viewModel.resetDiagnostics() }
                    )
                    3 -> SpecsTab()
                }
            }
        }
    }
}

// Reusable Custom Glassmorphic Card Container Component
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = FrostedStroke,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardCornerRadius = 24.dp
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(cardCornerRadius))
            .clickable(onClick = onClick)
    } else {
        modifier.clip(RoundedCornerShape(cardCornerRadius))
    }

    Column(
        modifier = cardModifier
            .background(DarkGlass)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.25f),
                        borderColor.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(cardCornerRadius)
            )
            .padding(18.dp),
        content = content
    )
}

// Reusable Glass Pill Badge
@Composable
fun GlassPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        )
    }
}

// Custom Glassmorphic Tabs Selection Bar
@Composable
fun GlassTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color(0x0EFFFFFF), shape = RoundedCornerShape(14.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val activeBg by animateColorAsState(
                targetValue = if (isSelected) Color(0x22FFFFFF) else Color.Transparent,
                animationSpec = tween(200), label = "TabColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) CyberTeal else Color.White.copy(alpha = 0.65f),
                animationSpec = tween(200), label = "TabTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(activeBg)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

// TAB 1: Live Dashboard
@Composable
fun DashboardTab(usbState: UsbStateInfo, viewModel: UsbViewModel) {
    val classifications by viewModel.classifications.collectAsState()
    val bestMatch = remember(usbState, classifications) {
        classifications.firstOrNull { it.compatibilityRating > 0.45 } ?: classifications.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // High Fidelity "Frosted Glass" Cable Hero Card
        item {
            val pulseTransition = rememberInfiniteTransition(label = "PulseEffect")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "PulseScale"
            )

            val animatedGlowSize = (100 + (pulseScale * 18)).dp
            val animatedGlowAlpha = 0.25f - (pulseScale * 0.15f)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (usbState.isConnected) CyberTeal else Color.White.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Connection Pulse Animation Layout
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (usbState.isConnected) {
                                // Pulsing Ring
                                Box(
                                    modifier = Modifier
                                        .size(animatedGlowSize)
                                        .background(
                                            color = CyberTeal.copy(alpha = animatedGlowAlpha),
                                            shape = CircleShape
                                        )
                                )
                            }
                            // Inner Glass Plate
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (usbState.isConnected) CyberTeal.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, // Connection sharing connectivity symbol
                                    contentDescription = "USB Connection Status Icon",
                                    tint = if (usbState.isConnected) CyberTeal else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dynamic cable detection naming
                        val titleText = if (usbState.isConnected) {
                            bestMatch?.name ?: "USB-C Interface Cable"
                        } else {
                            "No Probe Cable Connected"
                        }

                        val subTitleText = if (usbState.isConnected) {
                            bestMatch?.typicalWiringText ?: "Standard Connection Active"
                        } else {
                            "Plug in a physical or virtual connection to analyze channels"
                        }

                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.3).sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = subTitleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Capabilities Badges (Speed, Max power, alternate screen display support)
                        if (usbState.isConnected && bestMatch != null) {
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Speed Badge
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Speed Icon",
                                            tint = CyberTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "MAX SPEED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.4f),
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (bestMatch.maxSpeedGbps >= 1f) "${bestMatch.maxSpeedGbps.toInt()} Gbps" else "${(bestMatch.maxSpeedGbps * 1000).toInt()} Mbps",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // 2. Power delivery wattage constraints
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Power Rating",
                                            tint = CyberPink,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "MAX CHARGING",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.4f),
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${bestMatch.maxPowerWatts} Watts",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // 3. Alternate modes check
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Alt Mode",
                                            tint = CyberPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "ALT MODES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.4f),
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (bestMatch.videoAltModeSupported) "DisplayPort" else "None",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (bestMatch.videoAltModeSupported) CyberPurple else Color.White.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // High Speed Telemetry Power Gauge Grid Row
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "POWER DIAGNOSTICS",
                    color = CyberTeal,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PowerIndicatorGauge(
                        value = usbState.chargingPowerWatts,
                        maxLimit = 100.0,
                        unit = "W",
                        label = "Sensed Power",
                        color = CyberTeal,
                        modifier = Modifier.size(110.dp)
                    )

                    Column(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricParameterRow(
                            icon = Icons.Default.Info,
                            title = "Power Sourced",
                            value = "${String.format("%.2f", usbState.chargingPowerWatts)} W",
                            color = CyberTeal
                        )
                        MetricParameterRow(
                            icon = Icons.Default.PlayArrow,
                            title = "Live Current",
                            value = "${String.format("%.3f", usbState.chargingCurrentAmperes)} A",
                            color = Color.White
                        )
                        MetricParameterRow(
                            icon = Icons.Default.Check,
                            title = "Volts Measured",
                            value = "${String.format("%.2f", usbState.chargingVoltageVolts)} V",
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Connection Technical Specs Matrix Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HARDWARE TRANSCEIVER SIGNALS",
                    color = CyberPink,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Physical Port Connected", usbState.isConnected, CyberTeal)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Host/OTG Protocol", usbState.isHostConnected, CyberTeal)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Lanes Configured", usbState.isConfigured, CyberPink)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Port Unlocked", usbState.isUnlocked, CyberPink)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                    InteractiveParameterRow(
                        label = "Current Source Profile",
                        value = usbState.powerSource,
                        valueColor = if (usbState.isConnected) CyberTeal else Color.White
                    )

                    InteractiveParameterRow(
                        label = "Negotiated Voltage Cap",
                        value = if (usbState.maxVoltageVolts > 0) "${usbState.maxVoltageVolts}V" else "Detected Dynamically",
                        valueColor = Color.White
                    )

                    InteractiveParameterRow(
                        label = "Negotiated Current Cap",
                        value = if (usbState.maxCurrentAmperes > 0) "${usbState.maxCurrentAmperes}A" else "Detected Dynamically",
                        valueColor = Color.White
                    )

                    InteractiveParameterRow(
                        label = "Negotiated Wattage Profile",
                        value = if (usbState.maxPowerWatts > 0) "${usbState.maxPowerWatts}W Limit" else "Calculated Live",
                        valueColor = Color.White
                    )
                }
            }
        }

        // Host devices attached card (OTG capabilities check) & storage drive details
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "USB INTERFACE ACCESSORIES (${usbState.usbDevices.size})",
                        color = CyberPurple,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    GlassPill(
                        text = if (usbState.usbDevices.isNotEmpty()) "OTG ACTIVE" else "Host Empty",
                        color = if (usbState.usbDevices.isNotEmpty()) CyberTeal else Color.White.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VIRTUAL INTERFACE COUPLER SIMULATOR PANELS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(14.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "VIRTUAL STORAGE INSERTION SIMULATOR",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateStorageDevice(1) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal.copy(alpha = 0.2f), contentColor = CyberTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Kingston Duo", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.simulateStorageDevice(3) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple.copy(alpha = 0.2f), contentColor = CyberPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Samsung T7", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.simulateStorageDevice(2) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPink.copy(alpha = 0.2f), contentColor = CyberPink),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SanDisk OTG", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (usbState.usbDevices.any { it.isStorage }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearSimulatedDevices() },
                            modifier = Modifier.fillMaxWidth().height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, CyberPink.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPink),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("EJECT SIMULATED PHYSICAL DISK", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (usbState.usbDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty",
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No external USB devices detected via this connection.\nAttach an OTG storage device or legacy drive to inspect its hardware structures.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.45f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        usbState.usbDevices.forEach { dev ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = "Device Type",
                                            tint = if (dev.isStorage) CyberTeal else CyberPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (dev.isStorage) dev.product else "${dev.manufacturer} ${dev.product}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            if (dev.isStorage) {
                                                Text(
                                                    text = "Manufacturer: ${dev.manufacturer}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.5f)
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    GlassPill(
                                        text = if (dev.isStorage) "DISK DRIVE" else "ACCESSORY",
                                        color = if (dev.isStorage) CyberTeal else CyberPurple
                                    )
                                }

                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.06f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                // Specific storage drive parameters display as asked by user!
                                if (dev.isStorage) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("BRAND", dev.brand, CyberTeal)
                                            StorageInfoItem("USB PROTOCOL", dev.usbVersion, Color.White)
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("BUS BANDWIDTH", dev.speedClassString, Color.White)
                                            StorageInfoItem("FS FORMAT", dev.fileSystem, Color.White)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("CAPACITY", if (dev.storageCapacityGB >= 1024) "${dev.storageCapacityGB / 1024} Terabyte (TB)" else "${dev.storageCapacityGB} Gigabytes (GB)", CyberPink)
                                            StorageInfoItem(
                                                "DEVICE AGE", 
                                                "Mfg ${dev.releaseYear} (${dev.estimatedAgeYears} years old)", 
                                                if (dev.estimatedAgeYears > 8) CyberPink else CyberTeal
                                            )
                                        }
                                    }
                                } else {
                                    // standard non-storage parameters
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Class: ${dev.deviceClass}  |  Speed: ${dev.maxSpeedMbps} Mbps",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        )
                                        Text(
                                            text = "VID: ${dev.vendorId} | PID: ${dev.productId}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live battery metrics
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RECEIVING THERMAL & CAPACITY SENSORS",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Status",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f))
                        )
                        Text(
                            text = "${usbState.batteryLevel}% Charged",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Internal Temp",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f))
                        )
                        Text(
                            text = "${usbState.batteryTemperatureCelsius}°C",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (usbState.batteryTemperatureCelsius > 40) CyberPink else CyberTeal
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stability Status",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f))
                        )
                        Text(
                            text = if (usbState.batteryTemperatureCelsius < 36) "NOMINAL" else "WARNING: COMPRESS",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (usbState.batteryTemperatureCelsius < 36) CyberTeal else CyberPink
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageInfoItem(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = color
            )
        )
    }
}

// Power gauge drawing
@Composable
fun PowerIndicatorGauge(
    value: Double,
    maxLimit: Double,
    unit: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / maxLimit).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(500), label = "GaugeProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth) / 2

            // Draw base gray track
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Draw active neon track
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(color.copy(alpha = 0.3f), color, color)
                ),
                startAngle = 140f,
                sweepAngle = animatedProgress * 260f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${String.format("%.1f", value)}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.45f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MetricParameterRow(icon: ImageVector, title: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                .padding(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(14.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp))
        }
    }
}

@Composable
fun SignalCheckbox(label: String, checked: Boolean, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (checked) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(3.dp)
                )
                .border(
                    0.5.dp,
                    if (checked) color else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(color)
                        .align(Alignment.Center)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (checked) Color.White else Color.White.copy(alpha = 0.45f)
            )
        )
    }
}

@Composable
fun InteractiveParameterRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f))
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor
            )
        )
    }
}


// TAB 2: PROFILER (The Diagnostic Classification Wizard)
@Composable
fun ProfilerTab(
    usbState: UsbStateInfo,
    connectorType: Int,
    eMarkerIndicator: Int,
    videoAltMode: Int,
    perceivedSpeed: Int,
    classifications: List<CableClassification>,
    onSetConnector: (Int) -> Unit,
    onSetEmarker: (Int) -> Unit,
    onSetVideoAlt: (Int) -> Unit,
    onSetSpeed: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Physical Question Matrix Selector
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SPECIFICATION IDENTIFICATION MATRIX",
                    color = CyberTeal,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Connector Type
                SegmentedSelection(
                    title = "Physical Termination",
                    options = listOf("CC (Type-C to C)", "AC (Type-A to C)", "Legacy Converter"),
                    selectedIndex = connectorType,
                    onSelected = onSetConnector
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Emaker logo Checkbox
                SegmentedSelection(
                    title = "E-Marker Logo or High Capacity Stamp?",
                    options = listOf("Probing Cap", "100W/240W", "Standard 3A"),
                    selectedIndex = eMarkerIndicator,
                    onSelected = onSetEmarker
                )

                Spacer(modifier = Modifier.height(12.dp))

                // High res video test
                SegmentedSelection(
                    title = "Alternative Mode Display Capability",
                    options = listOf("Auto-Detect", "Active Screen", "Unwired / Fails"),
                    selectedIndex = videoAltMode,
                    onSelected = onSetVideoAlt
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Perceived File speed
                SegmentedSelection(
                    title = "Perceived Transfer Benchmark",
                    options = listOf("SuperSpeed (Gbps)", "Hi-Speed (Classic)", "Charge Only"),
                    selectedIndex = perceivedSpeed,
                    onSelected = onSetSpeed
                )
            }
        }

        // Probability results
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = CyberTeal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTIMATED SPEC MATCH PROBABILITY",
                        color = CyberTeal,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    GlassPill(
                        text = "WIZARD RATINGS",
                        color = CyberTeal
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    classifications.forEach { cls ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.0f).padding(end = 8.dp)) {
                                    Text(
                                        text = cls.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "${cls.typicalWiringText}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                Text(
                                    text = "${(cls.compatibilityRating * 100).toInt()}% Match",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (cls.compatibilityRating > 0.6) CyberTeal else Color.LightGray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Colored Progress bar representing probability
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(2.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(cls.compatibilityRating.toFloat())
                                        .height(4.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    CyberTeal.copy(alpha = 0.3f),
                                                    if (cls.compatibilityRating > 0.6) CyberTeal else CyberPurple
                                                )
                                            ),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Secondary metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Max Speed: ${if (cls.maxSpeedGbps >= 1f) "${cls.maxSpeedGbps.toInt()} Gbps" else "${(cls.maxSpeedGbps * 1000).toInt()} Mbps"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace)
                                )
                                Text(
                                    text = "Power: ${cls.maxPowerWatts}W Rated  |  Layout: ${cls.physicalPinsRequired}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Segmented Radio Buttons Styled neatly in Glassmorphic rows
@Composable
fun SegmentedSelection(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x06FFFFFF), shape = RoundedCornerShape(10.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(10.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            options.forEachIndexed { idx, value ->
                val isSelected = idx == selectedIndex
                val activeBgColor by animateColorAsState(
                    targetValue = if (isSelected) CyberTeal.copy(alpha = 0.15f) else Color.Transparent, label = "SegBg"
                )
                val activeBorderColor by animateColorAsState(
                    targetValue = if (isSelected) CyberTeal.copy(alpha = 0.3f) else Color.Transparent, label = "SegBorder"
                )
                val textCol by animateColorAsState(
                    targetValue = if (isSelected) CyberTeal else Color.White.copy(alpha = 0.5f), label = "SegText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeBgColor)
                        .border(0.5.dp, activeBorderColor, shape = RoundedCornerShape(8.dp))
                        .clickable { onSelected(idx) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textCol,
                            fontSize = 9.5.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}


// TAB 3: DIAGNOSTICS & HARDWARE SIGNAL GRAPH
@Composable
fun DiagnosticsTab(
    usbState: UsbStateInfo,
    diagnosticState: UsbViewModel.DiagnosticState,
    diagnosticResult: DiagnosticResult?,
    powerHistory: List<UsbViewModel.HistoryTick>,
    onStartSweep: () -> Unit,
    onResetSweep: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Neon Real-time Telemetry waveform generator
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HARMONIC WAVEFORM OSCILLOSCOPE",
                            color = CyberPink,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Tracking actual charging voltage & current load characteristics",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }

                    GlassPill(
                        text = "LIVE GRAPH",
                        color = CyberPink
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Canvas Telemetry Sparkline
                PowerSparkLineChart(
                    history = powerHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Avg Load: ${String.format("%.2f", if (powerHistory.isNotEmpty()) powerHistory.map { it.powerWatts }.average() else 0.0)}W",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = CyberTeal, fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "Current State: ${if (usbState.isConnected) "Oscillating @ ${String.format("%.2f", usbState.chargingVoltageVolts)}V" else "Telemetry Stalled"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }

        // Active Diagnostics run card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACTIVE PHYSICAL HARMONICS SWEEP",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (diagnosticState) {
                    UsbViewModel.DiagnosticState.Idle -> {
                        Text(
                            text = "Run full electrical characterization sweep diagnostics directly against the interface endpoints. Checks contact resistance, signal impedance noise, and voltage drop to isolate faulty joints.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = onStartSweep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_diagnostics_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = DeepSpace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RUN SIGNAL CHARACTERIZATION SWEEP",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DeepSpace
                                )
                            )
                        }
                    }

                    is UsbViewModel.DiagnosticState.Running -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = CyberPaintColorForProgress(diagnosticState.progress),
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = diagnosticState.step.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CyberTeal,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { diagnosticState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyberTeal,
                                trackColor = Color.White.copy(alpha = 0.08f),
                            )
                        }
                    }

                    UsbViewModel.DiagnosticState.Finished -> {
                        if (diagnosticResult != null) {
                            DiagnosticResultView(
                                result = diagnosticResult,
                                onReset = onResetSweep
                            )
                        }
                    }
                }
            }
        }
    }
}

// Function helper to animate diagnostic progress bar colors
@Composable
fun CyberPaintColorForProgress(progress: Float): Color {
    return when {
        progress < 0.4 -> CyberPurple
        progress < 0.85 -> CyberPink
        else -> CyberTeal
    }
}

// Beautiful Sparkline Canvas Drawing
@Composable
fun PowerSparkLineChart(
    history: List<UsbViewModel.HistoryTick>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas

        val margin = 8.dp.toPx()
        val width = size.width
        val height = size.height

        val maxPowerVal = history.map { it.powerWatts }.maxOrNull()?.coerceAtLeast(5.0) ?: 5.0
        val minPowerVal = 0.0

        val powerRange = maxPowerVal - minPowerVal

        val pointsCount = history.size
        val stepX = width / (pointsCount - 1)

        val strokePath = Path()
        val fillPath = Path()

        history.forEachIndexed { idx, tick ->
            val ratioY = ((tick.powerWatts - minPowerVal) / powerRange).toFloat()
            // Invert coordinate system as Canvas draws 0,0 from top-left
            val x = idx * stepX
            val y = height - (ratioY * (height - margin * 2) + margin)

            if (idx == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (idx == history.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw filled gradient area under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(CyberPink.copy(alpha = 0.15f), Color.Transparent)
            )
        )

        // Draw neon glow curves
        drawPath(
            path = strokePath,
            color = CyberPink,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw horizontal scale grids
        val gridLines = 3
        for (i in 0..gridLines) {
            val gridY = margin + (height - margin * 2) * (i / gridLines.toFloat())
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun DiagnosticResultView(
    result: DiagnosticResult,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SWEEP ANALYSIS REPORT",
                color = CyberTeal,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            )
            GlassPill(
                text = "SWEEP SUCCESS",
                color = CyberTeal
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Display classified name
        Text(
            text = result.classification,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Technical readings rows
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Contact Impedance", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f)))
                Text("${String.format("%.3f", result.estimatedResistanceOhms)} \u03A9", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = if (result.estimatedResistanceOhms < 0.12) CyberTeal else CyberPink, fontFamily = FontFamily.Monospace))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Signal Noise Line", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f)))
                Text("${String.format("%.1f", result.noiseLevelPct)}%", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Voltage Drop Stability", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f)))
                Text("${String.format("%.1f", result.voltageStabilityPct)}%", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = if (result.voltageStabilityPct > 95) CyberTeal else CyberPurple, fontFamily = FontFamily.Monospace))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Estimated Bandwidth Cap", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f)))
                Text(
                    text = if (result.throughputMbpsEst >= 1000.0) "${(result.throughputMbpsEst / 1000.0).toInt()} Gbps" else "${result.throughputMbpsEst.toInt()} Mbps",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = CyberTeal, fontFamily = FontFamily.Monospace)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diagnostic text remarks
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x06FFFFFF), shape = RoundedCornerShape(10.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Report Info",
                    tint = CyberTeal,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = result.remarks,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "CLEAR & RE-CALIBRATE PROBES",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


// TAB 4: TECHNICAL USB SPECIFICATION & PINOUT LAYOUT MAP
@Composable
fun SpecsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Physical Pin Map view drawing
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "USB TYPE-C PIN CONFIGURATION MAP",
                    color = CyberTeal,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "USB-C uses a reversible 24-pin design consisting of high-speed transmission lanes, physical configuration channels, backward USB 2.0 rails, and auxiliary controls.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        lineHeight = 15.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Render vector schematics of Type-C plug pins
                UsbTypeCPinoutCanvas(modifier = Modifier.fillMaxWidth().height(90.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PinoutCategoryRow("A1,A12,B1,B12", "GND", "Shielding grounding return path", Color.White.copy(alpha = 0.35f))
                    PinoutCategoryRow("A4,A9,B4,B9", "VBUS", "Power delivery bus rails (supports up to 48V/5A under EPR)", CyberTeal)
                    PinoutCategoryRow("A5,B5", "CC1, CC2", "Configuration Channel (Power handshakes with E-Marker)", CyberPink)
                    PinoutCategoryRow("A2,A3,B10,B11", "TX1+/-, RX1+/-", "SuperSpeed high frequency lanes (supports 5 / 10 / 20 / 40 Gbps)", CyberPurple)
                    PinoutCategoryRow("A6,A7,B6,B7", "D+/D-", "Legacy backward-compatible differential data rails", Color.White)
                }
            }
        }

        // Capabilities reference charts
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "USB POWER DELIVERY & SPEED MATRIX",
                    color = CyberPink,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StandardRow("USB 2.0 Legacy", "480 Mbps", "5V, 0.5A\u20141.5A", "2.5W\u20147.5W")
                    StandardRow("USB Type-C 1.2", "480 Mbps", "5V, 3A", "15 Watts")
                    StandardRow("USB-PD 2.0/3.0 Std", "10/20 Gbps", "5V/9V/15V/20V, 3A", "Up to 60 Watts")
                    StandardRow("USB-PD 3.0 E-Marked", "10/20 Gbps", "20 Volt, 5 Amp", "Up to 100 Watts")
                    StandardRow("USB-PD 3.1 Extended", "40 Gbps", "48 Volt, 5 Amp", "Up to 240 Watts (EPR)")
                }
            }
        }
    }
}

@Composable
fun PinoutCategoryRow(pins: String, label: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x06FFFFFF), shape = RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(5.dp))
                .border(0.5.dp, color.copy(alpha = 0.35f), shape = RoundedCornerShape(5.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        Column {
            Text(pins, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp))
            Text(desc, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, lineHeight = 14.sp))
        }
    }
}

@Composable
fun StandardRow(std: String, speed: String, voltAmp: String, watt: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(std, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp))
            Text(speed, style = MaterialTheme.typography.labelSmall.copy(color = CyberTeal, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(voltAmp, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace))
            Text(watt, style = MaterialTheme.typography.labelSmall.copy(color = CyberPink, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace))
        }
    }
}

// Custom Draw Canvas rendering of physical 24 pins connector layout
@Composable
fun UsbTypeCPinoutCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer metal collar casing
        drawRoundRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(0f, 10f),
            size = androidx.compose.ui.geometry.Size(w, h - 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(3.dp.toPx())
        )

        // Inner core tongue
        drawRoundRect(
            color = DarkGlass,
            topLeft = Offset(12.dp.toPx(), 18.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(w - 24.dp.toPx(), h - 36.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(12.dp.toPx(), 18.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(w - 24.dp.toPx(), h - 36.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(1.dp.toPx())
        )

        // Pins layout drawing
        val totalPinsCount = 12
        val startMargin = 22.dp.toPx()
        val spacing = (w - startMargin * 2) / (totalPinsCount - 1)

        for (i in 0 until totalPinsCount) {
            val pinX = startMargin + (i * spacing)
            val centerY = h / 2f

            // Alternate colors for standard pins groupings
            val pinColor = when (i) {
                0, 11 -> Color.White.copy(alpha = 0.35f) // GND
                3, 8 -> CyberTeal                      // VBUS
                1, 2, 9, 10 -> CyberPurple               // High Speed TX/RX Lanes
                4 -> CyberPink                         // CC lines
                5, 6 -> Color.White                    // D+/D- channels
                else -> Color.Gray
            }

            // Draw pin rectangle contacts
            drawRoundRect(
                color = pinColor,
                topLeft = Offset(pinX - 2.dp.toPx(), centerY - 5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(4.dp.toPx(), 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}
