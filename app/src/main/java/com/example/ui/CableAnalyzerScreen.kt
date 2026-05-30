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
import androidx.compose.material.icons.automirrored.filled.List
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

    val themeSelection by viewModel.themeSelection.collectAsState()
    val fontSelection by viewModel.fontSelection.collectAsState()
    val layoutSelection by viewModel.layoutSelection.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Live Dashboard, 1: Profiler, 2: Diagnostics, 3: Specs/Pinout
    var isShowCustomizer by remember { mutableStateOf(false) }

    // Dynamic color setup based on custom theme choice
    val themePrimary = when(themeSelection) {
        1 -> Color(0xFF10B981) // Neon Matrix Green (Emerald)
        2 -> Color(0xFFEC4899) // Cosmic Nebula (Deep Pink)
        3 -> Color(0xFFF59E0B) // Midnight Gold (Amber)
        else -> Color(0xFF60A5FA) // Hyper-Blue Neon (Default CyberTeal)
    }

    val themeSecondary = when(themeSelection) {
        1 -> Color(0xFF047857) // Emerald Dark/Medium
        2 -> Color(0xFF8B5CF6) // Purple Dark/Medium
        3 -> Color(0xFFD97706) // Bronze Medium
        else -> Color(0xFF818CF8) // Indigo (Default)
    }

    val themeAccent = when(themeSelection) {
        1 -> Color(0xFF34D399) // Mint
        2 -> Color(0xFFD946EF) // Magenta
        3 -> Color(0xFFFBBF24) // Gold Light
        else -> Color(0xFFF472B6) // Pink (Default)
    }

    val themeBackground = when(themeSelection) {
        1 -> Color(0xFF040705) // Matrix Deep Slate Dark
        2 -> Color(0xFF07040D) // Nebula Deep Slate Dark
        3 -> Color(0xFF080603) // Gold Charcoal Obsidian
        else -> Color(0xFF0F1115) // Deep Space Slate Dark (Default)
    }

    val themeFont = when(fontSelection) {
        1 -> FontFamily.Monospace // Tech Monospace
        2 -> FontFamily.SansSerif // Inter/Roboto Elegant Sans
        3 -> FontFamily.Serif // Serif Elegant
        else -> FontFamily.Default // Default premium rounded
    }

    // Dynamic animation scale durations (in millis)
    val baseDuration = when(animationSpeed) {
        1 -> 800 // Cinema Glide Slow-Mo
        2 -> 0   // Static Tech (Disabled)
        else -> 400 // Hyper-Expressive (Default)
    }

    // Background floating animated dots animation for depth/shimmer glassmorphism effect
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundParticles")
    val dotOffset1 by if (animationSpeed != 2) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 200f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (animationSpeed == 1) 14000 else 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Dot1"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    
    val dotOffset2 by if (animationSpeed != 2) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -180f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (animationSpeed == 1) 20000 else 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Dot2"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val currentTypography = MaterialTheme.typography.copy(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = themeFont),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = themeFont),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = themeFont),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = themeFont),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = themeFont),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = themeFont),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = themeFont),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = themeFont),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = themeFont),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = themeFont),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = themeFont),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = themeFont),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = themeFont),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = themeFont),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = themeFont)
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = themePrimary,
            secondary = themeSecondary,
            tertiary = themeAccent,
            background = themeBackground,
            surface = themeBackground
        ),
        typography = currentTypography
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Background radial cyber-glow gradients
                    drawRect(color = themeBackground)
                    if (layoutSelection != 1) { // Only draw glows if not raw Brutalist Stark style
                        // Theme Primary Glow top-left
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(themePrimary.copy(alpha = 0.28f), Color.Transparent),
                                center = Offset(size.width * -0.1f + dotOffset1 * 0.3f, size.height * -0.1f),
                                radius = size.width * 1.1f
                            )
                        )
                        // Theme Secondary Glow bottom-right
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(themeSecondary.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 1.1f + dotOffset2 * 0.3f, size.height * 1.1f),
                                radius = size.width * 1.0f
                            )
                        )
                        // Bio-Glow layout creates an extra pulsating dynamic center-left light sphere!
                        if (layoutSelection == 2) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(themeAccent.copy(alpha = 0.15f), Color.Transparent),
                                    center = Offset(size.width * 0.1f, size.height * 0.5f + dotOffset1 * 0.4f),
                                    radius = size.width * 0.7f
                                )
                            )
                        }
                    }
                },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
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
                                text = "ANALYZER PRO v3.0",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = themeAccent,
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
                                        .background(if (usbState.isConnected) themeAccent else Color.Gray)
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

                        // Top bar right actions: Connection pill + customize settings button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassPill(
                                text = if (usbState.isConnected) "PLUGGED" else "DISCONNECTED",
                                color = if (usbState.isConnected) themePrimary else Color.LightGray.copy(alpha = 0.6f)
                            )

                            IconButton(
                                onClick = { isShowCustomizer = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Preferences Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Tabs Bar selector
                    GlassTabBar(
                        tabs = listOf("DASHBOARD", "PROFILER", "DIAGNOSTICS", "PINOUT MAP"),
                        selectedIndex = activeTab,
                        themePrimary = themePrimary,
                        themeSecondary = themeSecondary,
                        layoutSelection = layoutSelection,
                        onTabSelected = { activeTab = it }
                    )

                    // Content Area dependent on active tab selection
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            if (baseDuration > 0) {
                                fadeIn(animationSpec = tween(baseDuration)) togetherWith fadeOut(animationSpec = tween(baseDuration))
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
                            }
                        },
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        label = "ActiveTabContent"
                    ) { targetIndex ->
                        when (targetIndex) {
                            0 -> DashboardTab(usbState, viewModel, themePrimary, themeSecondary, themeAccent, layoutSelection)
                            1 -> ProfilerTab(
                                usbState = usbState,
                                classifications = classifications,
                                themePrimary = themePrimary,
                                themeSecondary = themeSecondary,
                                themeAccent = themeAccent,
                                layoutSelection = layoutSelection
                            )
                            2 -> DiagnosticsTab(
                                usbState = usbState,
                                diagnosticState = diagnosticState,
                                diagnosticResult = diagnosticResult,
                                powerHistory = powerHistory,
                                themePrimary = themePrimary,
                                themeSecondary = themeSecondary,
                                themeAccent = themeAccent,
                                layoutSelection = layoutSelection,
                                onStartSweep = { viewModel.runDiagnosticSweep() },
                                onResetSweep = { viewModel.resetDiagnostics() }
                            )
                            3 -> SpecsTab(
                                usbState = usbState,
                                themePrimary = themePrimary,
                                themeSecondary = themeSecondary,
                                themeAccent = themeAccent,
                                layoutSelection = layoutSelection
                            )
                        }
                    }
                }

                // Elegant Custom Theme/Fonts/Layout Overlay Sheet/Dialog
                if (isShowCustomizer) {
                    CustomizerSheetOverlay(
                        themeSelection = themeSelection,
                        fontSelection = fontSelection,
                        layoutSelection = layoutSelection,
                        animationSpeed = animationSpeed,
                        themePrimary = themePrimary,
                        themeAccent = themeAccent,
                        themeBackground = themeBackground,
                        onSetTheme = { viewModel.setThemeSelection(it) },
                        onSetFont = { viewModel.setFontSelection(it) },
                        onSetLayout = { viewModel.setLayoutSelection(it) },
                        onSetSpeed = { viewModel.setAnimationSpeed(it) },
                        onDismiss = { isShowCustomizer = false }
                    )
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
    themePrimary: Color = Color(0xFF60A5FA),
    layoutSelection: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardCornerRadius = if (layoutSelection == 1) 0.dp else 24.dp
    val strokeWidth = if (layoutSelection == 1) 2.dp else 1.dp
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(cardCornerRadius))
            .clickable(onClick = onClick)
    } else {
        modifier.clip(RoundedCornerShape(cardCornerRadius))
    }

    Column(
        modifier = cardModifier
            .background(
                color = if (layoutSelection == 1) Color(0xFF161a22) else Color(0x0CFFFFFF),
                shape = RoundedCornerShape(cardCornerRadius)
            )
            .border(
                width = strokeWidth,
                brush = if (layoutSelection == 1) {
                    androidx.compose.ui.graphics.SolidColor(borderColor)
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.25f),
                            borderColor.copy(alpha = 0.04f)
                        )
                    )
                },
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
    themePrimary: Color = Color(0xFF60A5FA),
    themeSecondary: Color = Color(0xFF818CF8),
    layoutSelection: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    val containerCornerRadius = if (layoutSelection == 1) 0.dp else 14.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                color = if (layoutSelection == 1) Color(0xFF161A22) else Color(0x0EFFFFFF), 
                shape = RoundedCornerShape(containerCornerRadius)
            )
            .border(
                width = if (layoutSelection == 1) 2.dp else 0.5.dp, 
                color = if (layoutSelection == 1) themePrimary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.12f), 
                shape = RoundedCornerShape(containerCornerRadius)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val activeBg by animateColorAsState(
                targetValue = if (isSelected) {
                    if (layoutSelection == 1) themePrimary.copy(alpha = 0.2f) else Color(0x22FFFFFF)
                } else Color.Transparent,
                animationSpec = tween(200), label = "TabColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) themePrimary else Color.White.copy(alpha = 0.65f),
                animationSpec = tween(200), label = "TabTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(if (layoutSelection == 1) 0.dp else 10.dp))
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
fun DashboardTab(
    usbState: UsbStateInfo,
    viewModel: UsbViewModel,
    themePrimary: Color = Color(0xFF60A5FA),
    themeSecondary: Color = Color(0xFF818CF8),
    themeAccent: Color = Color(0xFFF472B6),
    layoutSelection: Int = 0
) {
    val classifications by viewModel.classifications.collectAsState()
    val bestMatch = remember(usbState, classifications) {
        classifications.firstOrNull { it.compatibilityRating > 0.45 } ?: classifications.firstOrNull()
    }
    val powerHistory by viewModel.powerHistory.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Section A: Cable Matrix Header
        item {
            Text(
                text = "⚡ CONNECTED CABLE ANALYSIS MATRIX",
                color = themePrimary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
        }

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

            val hasStorage = usbState.usbDevices.any { it.isStorage }
            val connectorType by viewModel.connectorType.collectAsState()

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (usbState.isConnected) themePrimary else Color.White.copy(alpha = 0.15f),
                themePrimary = themePrimary,
                layoutSelection = layoutSelection
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
                                            color = themePrimary.copy(alpha = animatedGlowAlpha),
                                            shape = CircleShape
                                        )
                                )
                            }
                            // Inner Glass Plate with Dynamic USB icons instead of general share button
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (usbState.isConnected) themePrimary.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                DynamicUsbIcon(
                                    isConnected = usbState.isConnected,
                                    connectorType = connectorType,
                                    isSuperSpeed = usbState.usbDevices.any { it.maxSpeedMbps > 440 } || hasStorage,
                                    isStorageConnected = hasStorage,
                                    themePrimary = themePrimary,
                                    themeAccent = themeAccent,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        // Sparking Flow Animation Line inside charging cable
                        if (usbState.isConnected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ACTIVE SIGNAL FLOW CHANNEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = themeAccent.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            FlowingElectronPaths(
                                isConnected = usbState.isConnected,
                                themePrimary = themePrimary,
                                themeAccent = themeAccent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

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
                                letterSpacing = (-0.5).sp
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
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                                            imageVector = Icons.Default.Lock, // Bandwidth cap icon
                                            contentDescription = "Speed Icon",
                                            tint = themePrimary,
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
                                            imageVector = Icons.Default.KeyboardArrowUp, // Watt level icon 
                                            contentDescription = "Power Rating",
                                            tint = themeAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "MAX POWER",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.4f),
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${bestMatch.maxPowerWatts}W Limit",
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
                                            tint = themeSecondary,
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
                                                color = if (bestMatch.videoAltModeSupported) themeSecondary else Color.White.copy(alpha = 0.6f)
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

        // Host devices attached card (OTG capabilities check) & storage drive details
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "USB ACCESSORY INTERFACE (${usbState.usbDevices.size})",
                        color = themeSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    GlassPill(
                        text = if (usbState.usbDevices.isNotEmpty()) "LINK ACTIVE" else "Empty Bus",
                        color = if (usbState.usbDevices.isNotEmpty()) themePrimary else Color.White.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                text = "No external USB devices detected via this connection.\nAttach an OTG storage device or legacy drive to inspect physical configurations.",
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
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Device Type",
                                            tint = if (dev.isStorage) themePrimary else themeSecondary,
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
                                        color = if (dev.isStorage) themePrimary else themeSecondary
                                    )
                                }

                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.06f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                // Specific storage drive parameters display
                                if (dev.isStorage) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("BRAND / CHIPSET ID", dev.brand, themePrimary)
                                            StorageInfoItem("USB PROTOCOL CLASS", dev.usbVersion, Color.White)
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("MAX BUS SPEED", dev.speedClassString, Color.White)
                                            StorageInfoItem("FILE SYSTEM FORMAT", dev.fileSystem, Color.White)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StorageInfoItem("SECTOR CAPACITY", if (dev.storageCapacityGB >= 1024) "${dev.storageCapacityGB / 1024} Terabyte (TB)" else "${dev.storageCapacityGB} Gigabytes (GB)", themeAccent)
                                            StorageInfoItem(
                                                "FABRICATIVE LIFETIME", 
                                                "Mfg ${dev.releaseYear} (${dev.estimatedAgeYears} years old)", 
                                                if (dev.estimatedAgeYears > 8) themeAccent else themePrimary
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

        // Section B: Beautiful Glowing Divider differentiating Phone specs & connected USB cable!
        item {
            GlowingNeonDivider(
                label = "📱 RECEIVING HANDSET SYSTEM TERMINAL",
                themePrimary = themePrimary,
                themeAccent = themeAccent
            )
        }

        // High Speed Telemetry Power Gauge Grid Row
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "POWER STATION COUPLER",
                        color = themePrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "DYNAMIC FEED",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

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
                        color = themePrimary,
                        modifier = Modifier.size(110.dp)
                    )

                    Column(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricParameterRow(
                            icon = Icons.Default.Info,
                            title = "Power Charged",
                            value = "${String.format("%.2f", usbState.chargingPowerWatts)} W",
                            color = themePrimary
                        )
                        MetricParameterRow(
                            icon = Icons.Default.PlayArrow,
                            title = "Current Draw",
                            value = "${String.format("%.3f", usbState.chargingCurrentAmperes)} A",
                            color = Color.White
                        )
                        MetricParameterRow(
                            icon = Icons.Default.Check,
                            title = "Voltage Rail",
                            value = "${String.format("%.2f", usbState.chargingVoltageVolts)} V",
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Explanatory Definition hints helping user digest phone stats purposes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ℹ️ UNDERSTANDING TELEMETRY CONTEXT",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "These numbers indicate how much electrical power is successfully crossing the connector boundaries. Resistance or sub-par conductor alignment restricts current flow (Amps) and forces a voltage drop across the pins.",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 13.sp)
                    )
                }
            }
        }

        // Connection Technical Specs Matrix Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HARDWARE TRANSCEIVER SIGNALS",
                    color = themeAccent,
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
                            SignalCheckbox("Physical Port Wired", usbState.isConnected, themePrimary)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Host/OTG Protocol", usbState.isHostConnected, themePrimary)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Lanes Configured", usbState.isConfigured, themeAccent)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SignalCheckbox("Port Guard Unlocked", usbState.isUnlocked, themeAccent)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                    InteractiveParameterRow(
                        label = "Power Sourcing Adapter",
                        value = usbState.powerSource,
                        valueColor = if (usbState.isConnected) themePrimary else Color.White
                    )

                    InteractiveParameterRow(
                        label = "Dynamic Voltage Cap",
                        value = if (usbState.maxVoltageVolts > 0) "${usbState.maxVoltageVolts}V max" else "Negotiated dynamically",
                        valueColor = Color.White
                    )

                    InteractiveParameterRow(
                        label = "Dynamic Current Cap",
                        value = if (usbState.maxCurrentAmperes > 0) "${usbState.maxCurrentAmperes}A max" else "Calculated line safety limit",
                        valueColor = Color.White
                    )

                    InteractiveParameterRow(
                        label = "Dynamic Power Threshold",
                        value = if (usbState.maxPowerWatts > 0) "${usbState.maxPowerWatts}W Limit" else "Monitored on continuous sweep",
                        valueColor = Color.White
                    )
                }
            }
        }

        // Receiving Thermal & Capacity Sensors (The battery stats)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THERMAL & PHYSICAL BATTERY SPECIFICATIONS",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "PHONE DYNAMICS",
                        color = themeSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

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
                            text = "${usbState.batteryLevel}% Status",
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
                            text = "${usbState.batteryTemperatureCelsius}°C Normal",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (usbState.batteryTemperatureCelsius > 40) themeAccent else themePrimary
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Safety Status",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.45f))
                        )
                        Text(
                            text = if (usbState.batteryTemperatureCelsius < 38) "NOMINAL SAFE" else "THERMAL LOCK",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (usbState.batteryTemperatureCelsius < 38) themePrimary else themeAccent
                            )
                        )
                    }
                }
            }
        }

        // Premium Real-time Power Consumptions Oscilloscope Graph at Bottom!
        item {
            BatteryTelemetryGraph(
                powerHistory = powerHistory,
                batteryLevel = usbState.batteryLevel,
                batteryTemp = usbState.batteryTemperatureCelsius,
                themePrimary = themePrimary,
                themeAccent = themeAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
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
                        .size(6.0.dp)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f)),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor,
                textAlign = TextAlign.End
            )
        )
    }
}

// Custom Dynamic USB physical representation icon
@Composable
fun DynamicUsbIcon(
    isConnected: Boolean,
    connectorType: Int,
    isSuperSpeed: Boolean,
    isStorageConnected: Boolean,
    themePrimary: Color,
    themeAccent: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSync")
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = size.minDimension / 1.9f
        val center = Offset(w / 2f, h / 2f)

        if (!isConnected) {
            // Sweep scan arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(themePrimary.copy(alpha = 0.40f), Color.Transparent)
                ),
                startAngle = angleRotation,
                sweepAngle = 110f,
                useCenter = true
            )
            // Center probe circle
            drawCircle(
                color = themePrimary,
                center = center,
                radius = 7.dp.toPx()
            )
            drawCircle(
                color = Color.White,
                center = center,
                radius = 3.dp.toPx()
            )
        } else if (isStorageConnected) {
            // Solid metal flash disk drive shape
            val widthDisk = 24.dp.toPx()
            val heightDisk = 42.dp.toPx()

            drawRoundRect(
                color = themeAccent.copy(alpha = 0.15f),
                topLeft = Offset(center.x - widthDisk / 2f, center.y - heightDisk / 2f),
                size = androidx.compose.ui.geometry.Size(widthDisk, heightDisk),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = themeAccent,
                topLeft = Offset(center.x - widthDisk / 2f, center.y - heightDisk / 2f),
                size = androidx.compose.ui.geometry.Size(widthDisk, heightDisk),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner flashing LED
            drawCircle(
                color = themePrimary,
                center = Offset(center.x, center.y + 10.dp.toPx()),
                radius = 2.5.dp.toPx()
            )

            // Outer plug
            drawRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(center.x - 7.dp.toPx(), center.y - heightDisk / 2f - 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 4.dp.toPx())
            )
        } else if (connectorType == 1) {
            // Legacy USB-A male core
            val boxW = 32.dp.toPx()
            val boxH = 26.dp.toPx()

            drawRoundRect(
                color = themePrimary.copy(alpha = 0.1f),
                topLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f),
                size = androidx.compose.ui.geometry.Size(boxW, boxH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = themePrimary,
                topLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f),
                size = androidx.compose.ui.geometry.Size(boxW, boxH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // SuperSpeed USB 3.0 Blue plastic core plate representation
            drawRect(
                color = Color(0xFF0D9488),
                topLeft = Offset(center.x - boxW / 2f + 4.dp.toPx(), center.y - boxH / 2f + 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(boxW - 8.dp.toPx(), 6.dp.toPx())
            )
        } else {
            // Symmetric physical Type-C plug illustration
            val pillW = 46.dp.toPx()
            val pillH = 22.dp.toPx()

            drawRoundRect(
                color = themePrimary.copy(alpha = 0.15f),
                topLeft = Offset(center.x - pillW / 2f, center.y - pillH / 2f),
                size = androidx.compose.ui.geometry.Size(pillW, pillH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillH / 2f, pillH / 2f)
            )
            drawRoundRect(
                color = themePrimary,
                topLeft = Offset(center.x - pillW / 2f, center.y - pillH / 2f),
                size = androidx.compose.ui.geometry.Size(pillW, pillH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillH / 2f, pillH / 2f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic core copper leaf
            drawRoundRect(
                color = themeAccent,
                topLeft = Offset(center.x - pillW / 2f + 8.dp.toPx(), center.y - 2.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(pillW - 16.dp.toPx(), 4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

// Breathtaking Charging Flow Electrodynamics Animation Waveform 
@Composable
fun FlowingElectronPaths(
    isConnected: Boolean,
    themePrimary: Color,
    themeAccent: Color,
    modifier: Modifier = Modifier
) {
    if (!isConnected) {
        Spacer(modifier = modifier)
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ElectronPhase")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlowPhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val points = 50
        val path = Path()

        for (i in 0..points) {
            val progress = i.toFloat() / points
            val x = progress * w
            val y = midY + sin(progress * 12f - flowPhase) * 6.dp.toPx()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw flowing neon cable wave line
        drawPath(
            path = path,
            color = themePrimary,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw scrolling glowing pulse dot representing electrons moving
        val electronProgress = (flowPhase / (2f * Math.PI.toFloat())) % 1f
        val eX = electronProgress * w
        val eY = midY + sin(electronProgress * 12f - flowPhase) * 6.dp.toPx()

        drawCircle(
            color = themeAccent,
            center = Offset(eX, eY),
            radius = 6.dp.toPx()
        )
        drawCircle(
            color = Color.White,
            center = Offset(eX, eY),
            radius = 2.5.dp.toPx()
        )
    }
}

// Section-separating neon line layout divider
@Composable
fun GlowingNeonDivider(
    label: String,
    themePrimary: Color,
    themeAccent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, themePrimary.copy(alpha = 0.4f))
                    )
                )
        )

        Box(
            modifier = Modifier
                .border(
                    width = 0.5.dp,
                    color = themeAccent.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                color = themeAccent,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(themePrimary.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
    }
}

// Advanced oscilloscope representation of power & battery statistics curve at bottom of dashboard
@Composable
fun BatteryTelemetryGraph(
    powerHistory: List<UsbViewModel.HistoryTick>,
    batteryLevel: Int,
    batteryTemp: Double,
    themePrimary: Color,
    themeAccent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(16.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYS REAL-TIME TELEMETRY VOLTAGE GRAPH",
                        color = themePrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "Pulsating physical current draws charted inside 1s updates",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }

                // Small battery system container badge
                Box(
                    modifier = Modifier
                        .background(themeAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                        .border(0.5.dp, themeAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HEALTH: $batteryLevel% | ${batteryTemp}°C",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = themeAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Graph canvas area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (powerHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AWAITING ACTIVE DATA PROBES FEED...",
                            color = Color.White.copy(alpha = 0.2f),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                } else {
                    val maxVal = remember(powerHistory) {
                        (powerHistory.maxOfOrNull { it.powerWatts } ?: 5.0).coerceAtLeast(10.0)
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pointsCount = powerHistory.size
                        val stepX = w / (pointsCount - 1).coerceAtLeast(1)

                        val path = Path()
                        val fillPath = Path()

                        powerHistory.forEachIndexed { idx, tick ->
                            val x = idx * stepX
                            val normalizeWatts = (tick.powerWatts / maxVal).coerceIn(0.0, 1.0)
                            val y = h - (normalizeWatts * h * 0.82).toFloat() - 4.dp.toPx()

                            if (idx == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, h)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (idx == pointsCount - 1) {
                                fillPath.lineTo(x, h)
                            }
                        }

                        // Gradient underlying filling curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(themePrimary.copy(alpha = 0.15f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Highlight vector stroke
                        drawPath(
                            path = path,
                            color = themePrimary,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // High fidelity active pulsing coordinate endpoint
                        if (pointsCount > 0) {
                            val lastIdx = pointsCount - 1
                            val tick = powerHistory[lastIdx]
                            val normalizeWatts = (tick.powerWatts / maxVal).coerceIn(0.0, 1.0)
                            val finalX = lastIdx * stepX
                            val finalY = h - (normalizeWatts * h * 0.82).toFloat() - 4.dp.toPx()

                            drawCircle(
                                color = themeAccent,
                                center = Offset(finalX, finalY),
                                radius = 5.dp.toPx()
                            )
                            drawCircle(
                                color = Color.White,
                                center = Offset(finalX, finalY),
                                radius = 2.dp.toPx()
                            )
                        }

                        // Horizontal base coordinate markings
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, h),
                            end = Offset(w, h),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-metrics descriptions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "30s Telemetry Range Frame",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Color.White.copy(alpha = 0.35f))
                )

                val lastPower = if (powerHistory.isNotEmpty()) powerHistory.last().powerWatts else 0.0
                Text(
                    text = "Active Waveform Load: ${String.format("%.2f", lastPower)}W",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        color = themePrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Live Port Probing",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}


// TAB 2: PROFILER (The Diagnostic Classification Wizard)
@Composable
fun ProfilerTab(
    usbState: UsbStateInfo,
    classifications: List<CableClassification>,
    themePrimary: Color = Color(0xFF60A5FA),
    themeSecondary: Color = Color(0xFF818CF8),
    themeAccent: Color = Color(0xFFF472B6),
    layoutSelection: Int = 0
) {
    // Derive the values in the UI for display based on automated judgment
    val isCc = if (usbState.isConnected) {
        (usbState.chargingPowerWatts > 15.0) || (usbState.powerSource == "AC Fast Charger") || (usbState.chargingCurrentAmperes > 1.5) || usbState.usbDevices.any { it.maxSpeedMbps > 440 }
    } else {
        true
    }
    
    val emarkYes = usbState.isConnected && isCc && (
        usbState.chargingPowerWatts > 60.0 || usbState.maxPowerWatts > 60.0 || usbState.usbDevices.any { it.maxSpeedMbps > 440 }
    )
    
    val videoYes = usbState.isConnected && emarkYes && isCc
    
    val speedFast = usbState.isConnected && (
        usbState.usbDevices.any { it.maxSpeedMbps > 440 } || usbState.chargingPowerWatts > 18.0
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Active HW Probe Grid
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), themePrimary = themePrimary, layoutSelection = layoutSelection, borderColor = themePrimary) {
                Text(
                    text = "HARDWARE TELEMETRY SCANNER [AUTOMATED]",
                    color = themePrimary,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProbeRowItem(
                        title = "Termination Interface",
                        value = if (usbState.isConnected) {
                            if (isCc) "Type-C to Type-C Core Sensed (Dual Configuration Channels Active)" 
                            else "Type-A to Type-C Legacy Sensed (56kΩ safety pull-up verified)"
                        } else "Coupler standby (Reference signature loaded)",
                        status = usbState.isConnected,
                        themeAccent = themeAccent
                    )

                    ProbeRowItem(
                        title = "E-Marker Spec silicon",
                        value = if (usbState.isConnected) {
                            if (emarkYes) "VERIFIED SIGNATURE (E-Marker CPU responding, EPR 240W capabilities unlocked)" 
                            else "COPPER HIGH-CAP STANDARD (Standard passive pass, capped at 60W/3A)"
                        } else "Coupler standby (Reference signature loaded)",
                        status = emarkYes,
                        themeAccent = themeAccent
                    )

                    ProbeRowItem(
                        title = "Video Payload Transceiver (Alt Mode)",
                        value = if (usbState.isConnected) {
                            if (videoYes) "VERIFIED SUPPORT (Lanes A2, A3, B10, B11 mapped for DP Alternate Video payload)" 
                            else "UNSUPPORTED (High-frequency lanes restricted for generic high speed sync only)"
                        } else "Coupler standby (Reference signature loaded)",
                        status = videoYes,
                        themeAccent = themeAccent
                    )

                    ProbeRowItem(
                        title = "Core Bus Synchronous Benchmark",
                        value = if (usbState.isConnected) {
                            if (speedFast) "SuperSpeed Subsystem Negotiated (up to 20Gbps lane layout configured)" 
                            else "Legacy USB 2.0 Subsystem Negotiated (480Mbps transport cap applied)"
                        } else "Coupler standby (Reference signature loaded)",
                        status = speedFast,
                        themeAccent = themeAccent
                    )
                }
            }
        }

        // Probability results
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = themePrimary, themePrimary = themePrimary, layoutSelection = layoutSelection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTIMATED SPEC MATCH PROBABILITY",
                        color = themePrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    GlassPill(
                        text = "WIZARD RATINGS",
                        color = themePrimary
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
                                        color = if (cls.compatibilityRating > 0.6) themePrimary else Color.LightGray
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
                                                    themePrimary.copy(alpha = 0.3f),
                                                    if (cls.compatibilityRating > 0.6) themePrimary else themeSecondary
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

// Reusable Automated Diagnostic Probe Row Item
@Composable
fun ProbeRowItem(
    title: String,
    value: String,
    status: Boolean,
    themeAccent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(10.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (status) themeAccent else Color.White.copy(alpha = 0.15f))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.sp
            )
        )
    }
}


// TAB 3: DIAGNOSTICS & HARDWARE SIGNAL GRAPH
@Composable
fun DiagnosticsTab(
    usbState: UsbStateInfo,
    diagnosticState: UsbViewModel.DiagnosticState,
    diagnosticResult: DiagnosticResult?,
    powerHistory: List<UsbViewModel.HistoryTick>,
    themePrimary: Color = Color(0xFF60A5FA),
    themeSecondary: Color = Color(0xFF818CF8),
    themeAccent: Color = Color(0xFFF472B6),
    layoutSelection: Int = 0,
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
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = themeAccent, themePrimary = themePrimary, layoutSelection = layoutSelection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HARMONIC WAVEFORM OSCILLOSCOPE",
                            color = themeAccent,
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
                        color = themeAccent
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
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = themePrimary, fontFamily = FontFamily.Monospace)
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
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = themePrimary, themePrimary = themePrimary, layoutSelection = layoutSelection) {
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
                            colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
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
fun SpecsTab(
    usbState: UsbStateInfo,
    themePrimary: Color = Color(0xFF60A5FA),
    themeSecondary: Color = Color(0xFF818CF8),
    themeAccent: Color = Color(0xFFF472B6),
    layoutSelection: Int = 0
) {
    val isConnected = usbState.isConnected
    val isCc = isConnected && (
        usbState.chargingPowerWatts > 15.0 || usbState.powerSource == "AC Fast Charger" || usbState.chargingCurrentAmperes > 1.5 || usbState.usbDevices.any { it.maxSpeedMbps > 440 }
    )
    val isSuperSpeed = isConnected && (
        usbState.usbDevices.any { it.maxSpeedMbps > 440 } || usbState.chargingPowerWatts > 18.0
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Physical Pin Map view drawing
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = themePrimary, themePrimary = themePrimary, layoutSelection = layoutSelection) {
                Text(
                    text = "USB TYPE-C PIN CONFIGURATION MAP",
                    color = themePrimary,
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
                UsbTypeCPinoutCanvas(
                    usbState = usbState,
                    themePrimary = themePrimary,
                    themeSecondary = themeSecondary,
                    themeAccent = themeAccent,
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PinoutCategoryRow(
                        pins = "A1,A12,B1,B12",
                        label = "GND",
                        desc = "Shielding grounding return path and complete current loop return safety channels.",
                        statusText = if (isConnected) "ACTIVE / COMPLETED" else "DISCONNECTED",
                        isActive = isConnected,
                        color = Color.White,
                        themeAccent = themeAccent
                    )
                    PinoutCategoryRow(
                        pins = "A4,A9,B4,B9",
                        label = "VBUS",
                        desc = "Power delivery bus rails (senses up to 48V/5A under Extended Power Range handshakes).",
                        statusText = if (isConnected) "ACTIVE (${String.format("%.1f", usbState.chargingVoltageVolts)}V RAIL)" else "DISCONNECTED",
                        isActive = isConnected,
                        color = themePrimary,
                        themeAccent = themeAccent
                    )
                    PinoutCategoryRow(
                        pins = "A5,B5",
                        label = "CC1,CC2",
                        desc = "Configuration Channel used for cable orientation discovery, power handshakes, and E-marking communication.",
                        statusText = if (isConnected) (if (isCc) "ACTIVE (NEGOTIATED)" else "LEGACY INLINE") else "DISCONNECTED",
                        isActive = isConnected,
                        color = themeAccent,
                        themeAccent = themeAccent
                    )
                    PinoutCategoryRow(
                        pins = "A2,A3,B10,B11",
                        label = "TX+/-,RX+/-",
                        desc = "Ultra-frequency differential lanes for high-speed capabilities: supports up to 40 Gbps on SuperSpeed buses.",
                        statusText = if (isSuperSpeed) "ACTIVE (SUPERSPEED)" else if (isConnected) "INACTIVE (USB 2.0 STANDARD)" else "DISCONNECTED",
                        isActive = isSuperSpeed,
                        color = themeSecondary,
                        themeAccent = themeAccent
                    )
                    PinoutCategoryRow(
                        pins = "A6,A7,B6,B7",
                        label = "D+/D-",
                        desc = "Legacy backward-compatible differential data multiplexing channels, active by default on simple lines.",
                        statusText = if (isConnected) "ACTIVE (MULTIPLEXED)" else "DISCONNECTED",
                        isActive = isConnected,
                        color = Color(0xFF34D399),
                        themeAccent = themeAccent
                    )
                }
            }
        }

        // Capabilities reference charts
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = themeSecondary, themePrimary = themePrimary, layoutSelection = layoutSelection) {
                Text(
                    text = "USB POWER DELIVERY & SPEED MATRIX",
                    color = themeAccent,
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
                    StandardRow("USB 2.0 Legacy", "480 Mbps", "5V, 0.5A\u20141.5A", "2.5W\u20147.5W", themePrimary, themeAccent)
                    StandardRow("USB Type-C 1.2", "480 Mbps", "5V, 3A", "15 Watts", themePrimary, themeAccent)
                    StandardRow("USB-PD 2.0/3.0 Std", "10/20 Gbps", "5V/9V/15V/20V, 3A", "Up to 60 Watts", themePrimary, themeAccent)
                    StandardRow("USB-PD 3.0 E-Marked", "10/20 Gbps", "20 Volt, 5 Amp", "Up to 100 Watts", themePrimary, themeAccent)
                    StandardRow("USB-PD 3.1 Extended", "40 Gbps", "48 Volt, 5 Amp", "Up to 240 Watts (EPR)", themePrimary, themeAccent)
                }
            }
        }
    }
}

@Composable
fun PinoutCategoryRow(
    pins: String,
    label: String,
    desc: String,
    statusText: String,
    isActive: Boolean,
    color: Color,
    themeAccent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x06FFFFFF), shape = RoundedCornerShape(12.dp))
            .border(
                0.5.dp, 
                if (isActive) color.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f), 
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .background(
                            if (isActive) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), 
                            shape = RoundedCornerShape(5.dp)
                        )
                        .border(
                            0.5.dp, 
                            if (isActive) color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f), 
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            color = if (isActive) color else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                
                Text(
                    text = pins, 
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Connection badge status text
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f), 
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            0.5.dp, 
                            if (isActive) color.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f), 
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) color else Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = desc, 
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.55f), 
                    fontSize = 10.sp, 
                    lineHeight = 14.sp
                )
            )
        }
    }
}

@Composable
fun StandardRow(std: String, speed: String, voltAmp: String, watt: String, themePrimary: Color, themeAccent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(std, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp))
            Text(speed, style = MaterialTheme.typography.labelSmall.copy(color = themePrimary, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(voltAmp, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace))
            Text(watt, style = MaterialTheme.typography.labelSmall.copy(color = themeAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace))
        }
    }
}

// Custom Draw Canvas rendering of physical 24 pins connector layout
@Composable
fun UsbTypeCPinoutCanvas(
    usbState: UsbStateInfo,
    themePrimary: Color,
    themeSecondary: Color,
    themeAccent: Color,
    modifier: Modifier = Modifier
) {
    // Derive connection properties
    val isConnected = usbState.isConnected
    val isCc = if (isConnected) {
        (usbState.chargingPowerWatts > 15.0) || (usbState.powerSource == "AC Fast Charger") || (usbState.chargingCurrentAmperes > 1.5) || usbState.usbDevices.any { it.maxSpeedMbps > 440 }
    } else {
        true
    }
    
    val isSuperSpeed = isConnected && (
        usbState.usbDevices.any { it.maxSpeedMbps > 440 } || usbState.chargingPowerWatts > 18.0
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PinPulse")
    val sparkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SparkPhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer metal collar casing
        drawRoundRect(
            color = if (isConnected) themePrimary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
            topLeft = Offset(0f, 10f),
            size = androidx.compose.ui.geometry.Size(w, h - 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(3.dp.toPx())
        )

        // Inner core tongue
        drawRoundRect(
            color = if (isConnected) themePrimary.copy(alpha = 0.05f) else DarkGlass,
            topLeft = Offset(12.dp.toPx(), 18.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(w - 24.dp.toPx(), h - 36.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawRoundRect(
            color = if (isConnected) themePrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
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

            // Check if pin group is active
            val isPinActive = when (i) {
                0, 11 -> isConnected // GND is active when connected 
                3, 8 -> isConnected // VBUS is active when connected
                4 -> isConnected // CC lines are active when connected
                5, 6 -> isConnected // D+/D- lines are active when connected
                1, 2, 9, 10 -> isSuperSpeed // High speed lanes are only active on SuperSpeed
                else -> false
            }

            // Pin grouping colors
            val pinColor = when (i) {
                0, 11 -> if (isPinActive) Color.White else Color.White.copy(alpha = 0.15f) // GND
                3, 8 -> if (isPinActive) themePrimary else themePrimary.copy(alpha = 0.15f) // VBUS
                1, 2, 9, 10 -> if (isPinActive) themeSecondary else themeSecondary.copy(alpha = 0.15f) // High Speed RX/TX lanes
                4 -> if (isPinActive) themeAccent else themeAccent.copy(alpha = 0.15f) // CC lines
                5, 6 -> if (isPinActive) Color(0xFF34D399) else Color(0xFF34D399).copy(alpha = 0.15f) // D+/D- Mint color
                else -> Color.Gray.copy(alpha = 0.15f)
            }

            // Draw pin rectangle contacts
            drawRoundRect(
                color = pinColor,
                topLeft = Offset(pinX - 2.5.dp.toPx(), centerY - 6.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(5.dp.toPx(), 12.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )

            // Draw spark ripple animation if active
            if (isPinActive) {
                val pointPulseOffset = (sparkPhase / 100f) * 12.dp.toPx()
                drawCircle(
                    color = pinColor.copy(alpha = 0.35f),
                    center = Offset(pinX, centerY - 6.dp.toPx() + pointPulseOffset),
                    radius = 2.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun CustomizerSheetOverlay(
    themeSelection: Int,
    fontSelection: Int,
    layoutSelection: Int,
    animationSpeed: Int,
    themePrimary: Color,
    themeAccent: Color,
    themeBackground: Color,
    onSetTheme: (Int) -> Unit,
    onSetFont: (Int) -> Unit,
    onSetLayout: (Int) -> Unit,
    onSetSpeed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss) // Click outside to dismiss
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dialog container Card
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clickable(enabled = false) {} // Prevent click-through
                .background(
                    color = Color(0xFF161a23),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = themePrimary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Preferences",
                        tint = themeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PREFERENCES TUNER",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Visual Theme
                item {
                    Text(
                        text = "VISUAL SCHEME",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomRadioRow(
                        selectedIndex = themeSelection,
                        options = listOf("Default CyberTeal", "Neon Matrix Green", "Cosmic Purple/Pink", "Midnight Gold"),
                        onSelect = onSetTheme,
                        activeColor = themePrimary
                    )
                }

                // Section 2: Layout Type
                item {
                    Text(
                        text = "PREDEFINED INTERFACE LAYOUT",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomRadioRow(
                        selectedIndex = layoutSelection,
                        options = listOf("Standard Glassmorphic", "Box Brutalist Stark", "Bio-Glow Liquid"),
                        onSelect = onSetLayout,
                        activeColor = themePrimary
                    )
                }

                // Section 3: Font Styles
                item {
                    Text(
                        text = "DISPLAY TYPOGRAPHY",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomRadioRow(
                        selectedIndex = fontSelection,
                        options = listOf("Default UI Round", "Tech Monospace", "Elegant Sans", "Classic Serif"),
                        onSelect = onSetFont,
                        activeColor = themePrimary
                    )
                }

                // Section 4: Transitions Speed
                item {
                    Text(
                        text = "MOTION TRANSITIONS SPEED",
                        color = themeAccent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomRadioRow(
                        selectedIndex = animationSpeed,
                        options = listOf("Hyper-Expressive", "Cinema Slow Glide", "Static Performance"),
                        onSelect = onSetSpeed,
                        activeColor = themePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themePrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "APPLY PREFERENCES CONFIG",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

@Composable
fun CustomRadioRow(
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit,
    activeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Radio circular bubble
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}
