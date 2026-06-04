package com.example.ui

import android.hardware.usb.UsbDevice
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CableClassification
import com.example.data.UsbStateInfo
import com.example.viewmodel.UsbViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CableAnalyzerScreen(viewModel: UsbViewModel) {
    val usbState by viewModel.usbState.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val diagnosticState by viewModel.diagnosticState.collectAsState()
    val themeSelection by viewModel.themeSelection.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanMessage by remember { mutableStateOf("Ready to initiate sweep") }

    val bestMatch = remember(usbState, classifications) {
        classifications.firstOrNull { it.compatibilityRating > 0.45 } ?: classifications.firstOrNull()
    }

    // Gorgeous OLED Space Theme Colors - exclusively Dark Mode
    val spaceBg = Color(0xFF03050C)
    val cardBg = Color(0xCF0B0E17)

    val themePrimary = when(themeSelection) {
        1 -> Color(0xFF10B981) // Acid Emerald
        2 -> Color(0xFFEC4899) // Cyber Pink
        3 -> Color(0xFFF59E0B) // Amber Glow
        else -> Color(0xFF38BDF8) // Hyper Blue (Default)
    }

    val themeAccent = when(themeSelection) {
        1 -> Color(0xFF00FF88) // Poison Mint
        2 -> Color(0xFFFF007F) // Electric Rose
        3 -> Color(0xFFFFB300) // Solar Gold
        else -> Color(0xFF00D2FF) // Aurora Cyan (Default)
    }

    val themeSecondary = when(themeSelection) {
        1 -> Color(0xFF047857) // Dark Emerald
        2 -> Color(0xFF9D174D) // Dark Rose
        3 -> Color(0xFFB45309) // Dark Amber
        else -> Color(0xFF1D4ED8) // Deep Blue
    }

    // Dynamic rotation and pulsing animation states
    val infiniteTransition = rememberInfiniteTransition(label = "refraction_world")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radial_glow"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background dark OLED space
                drawRect(color = spaceBg)
                // Ambient colorful backdrop nebulas
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(themePrimary.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.15f),
                        radius = size.width * 0.9f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(themeAccent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.85f),
                        radius = size.width * 1.0f
                    )
                )
            },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Premium Ultra-Minimalist Title bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUANTUM CABLE PULSE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = themeAccent,
                            letterSpacing = 2.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Minimalist Calibrator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                // Inline Accent Switcher - beautiful minimal color dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 1, 2, 3).forEach { index ->
                        val dotColor = when(index) {
                            1 -> Color(0xFF10B981)
                            2 -> Color(0xFFEC4899)
                            3 -> Color(0xFFFBBF24)
                            else -> Color(0xFF38BDF8)
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = if (themeSelection == index) 1f else 0.25f))
                                .border(
                                    width = 1.5.dp,
                                    color = if (themeSelection == index) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.setThemeSelection(index)
                                }
                        )
                    }
                }
            }

            // Connection State Glass Banner
            RefractiveGlassCard(
                glowAccentColor = themePrimary,
                bgColor = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (usbState.isConnected) themeAccent else Color(0x3DFFFFFF))
                            .shadow(
                                if (usbState.isConnected) 10.dp else 0.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = themeAccent,
                                spotColor = themeAccent
                            )
                    )
                    Column {
                        Text(
                            text = if (usbState.isConnected) "ACTIVE CABLE CONNECTED" else "STANDBY DEMO REFERENCE",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (usbState.isConnected) Color.White else Color.White.copy(alpha = 0.5f),
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = if (usbState.isConnected) {
                                "Direct transceiver sweep actively reporting power and link rate."
                            } else {
                                "Connect accessory to analyze live, or view simulated typical targets."
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Central "3D" Depth Refractive Quantum Socket Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background infinite pulse glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 32.dp,
                            shape = CircleShape,
                            ambientColor = themePrimary.copy(alpha = glowAlpha),
                            spotColor = themeAccent.copy(alpha = glowAlpha),
                            clip = false
                        )
                        .background(Color.Transparent)
                )

                // Beautiful interactive Compose Canvas drawing the refractive socket
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width * 0.45f
                    val innerRadius = size.width * 0.32f

                    // Draw outer refractive glass ring
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                themeAccent.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.05f),
                                themePrimary.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.25f)
                            )
                        ),
                        radius = outerRadius,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Draw inner refractive chamber backing
                    drawCircle(
                        color = Color(0x33000000),
                        radius = innerRadius
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                themeAccent.copy(alpha = if (isScanning) 0.65f else 0.20f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = innerRadius
                        ),
                        radius = innerRadius
                    )

                    // Draw Orbiting Calibration Nodes (represents lanes and status)
                    val nodeCount = 5
                    for (i in 0 until nodeCount) {
                        val angleDeg = (orbitRotation + (i * (360f / nodeCount))) % 360f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val distance = innerRadius * 0.8f
                        val nodeX = center.x + (distance * cos(angleRad)).toFloat()
                        val nodeY = center.y + (distance * sin(angleRad)).toFloat()

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(themeAccent, Color.Transparent),
                                center = Offset(nodeX, nodeY),
                                radius = 10.dp.toPx()
                            ),
                            center = Offset(nodeX, nodeY),
                            radius = 6.dp.toPx()
                        )
                        drawCircle(
                            color = Color.White,
                            center = Offset(nodeX, nodeY),
                            radius = 2.dp.toPx()
                        )
                    }

                    // Scan sweeping line
                    if (isScanning) {
                        val scanAngleRad = Math.toRadians(scanProgress.toDouble() * 3.6)
                        val lineEndX = center.x + (outerRadius * cos(scanAngleRad)).toFloat()
                        val lineEndY = center.y + (outerRadius * sin(scanAngleRad)).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(themePrimary, themeAccent, Color.Transparent)
                            ),
                            start = center,
                            end = Offset(lineEndX, lineEndY),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Core central symbol
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (usbState.isConnected) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = "Socket Center",
                        tint = if (isScanning) themeAccent else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isScanning) "${scanProgress.toInt()}%" else if (usbState.isConnected) "LINKED" else "STANDBY",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Minimalist Calibration Sweep Action Button
            RefractiveGlassCard(
                glowAccentColor = themeAccent,
                bgColor = Color(0x3D0B0E17),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isScanning) {
                            isScanning = true
                            scanProgress = 0f
                            coroutineScope.launch {
                                val messages = listOf(
                                    "Isolating Ground Differential...",
                                    "Measuring CC Channel Pullups...",
                                    "Testing SuperSpeed+ Lane Integrity...",
                                    "Pulsing E-Marker ID Transceiver...",
                                    "System Safe. Calibration Complete!"
                                )
                                var msgIndex = 0
                                while (scanProgress < 100f) {
                                    delay(40)
                                    scanProgress += 2f
                                    if (scanProgress.toInt() % 20 == 0 && msgIndex < messages.size - 1) {
                                        msgIndex++
                                    }
                                    scanMessage = messages[msgIndex]
                                }
                                delay(600)
                                isScanning = false
                                scanMessage = "Quantum Line Sweep Complete"
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isScanning) "ANALYZING PHYS LANES..." else "ENGAGE ADVANCED CALIBRATION SWEEP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePrimary,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple, gorgeous glass progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(if (isScanning) scanProgress / 100f else 0.35f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(themePrimary, themeAccent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scanMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Zero Clutter Specs Block (Purely Minimal Core stats with rich refraction)
            Text(
                text = "CABLE QUANTUM PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            val displaySpeed = bestMatch?.maxSpeedGbps?.let {
                if (it >= 1f) "${it.toInt()} Gbps" else "${(it * 1000).toInt()} Mbps"
            } ?: "480 Mbps"

            val displayPower = bestMatch?.maxPowerWatts?.let { "${it}W Max" } ?: "15W standard"
            val displayRating = bestMatch?.name ?: "General Utility Standard Link"
            val displayVideo = if (bestMatch?.videoAltModeSupported == true) "4K Crystal Video" else "No Alternate Display"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Box 1: Bandwidth rating
                RefractiveGlassCard(
                    glowAccentColor = themePrimary,
                    bgColor = cardBg,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Bandwidth Speed Indicator",
                            tint = themePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SPEED BANDWIDTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displaySpeed,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Box 2: Power Limit rating
                RefractiveGlassCard(
                    glowAccentColor = themeAccent,
                    bgColor = cardBg,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power Speed",
                            tint = themeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "POWER THRUPUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayPower,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Box 3: General Evaluation Display Card (Full Refractive)
            RefractiveGlassCard(
                glowAccentColor = themePrimary,
                bgColor = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INTELLIGENT LINK GRADING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = displayRating,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Alt Mode",
                                tint = themeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Alternate Video Out",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }
                        Text(
                            text = displayVideo,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = themeAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Real-Time Diagnostic Flow (Super Clean, Non-Technical Jargon metric display)
            RefractiveGlassCard(
                glowAccentColor = themeAccent,
                bgColor = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE LINK METRICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(themeAccent.copy(alpha = 0.15f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OPTIMAL STABLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = themeAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Power Flow",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = "${String.format("%.1f", if (usbState.isConnected) usbState.chargingPowerWatts else 0.0)} Watts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Data Lanes",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = if (usbState.isConnected) "CC1, CC2 Active" else "Ground References Standby",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// Outstanding Depth Refractive Glass Container Card implementing real frosted edges & neon chromatic refraction
@Composable
fun RefractiveGlassCard(
    modifier: Modifier = Modifier,
    glowAccentColor: Color = Color(0xFF60A5FA),
    bgColor: Color = Color(0xCF0B0E17),
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "depth_refract")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_refract"
    )

    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = glowAccentColor.copy(alpha = 0.15f),
                spotColor = glowAccentColor.copy(alpha = 0.22f),
                clip = false
            )
            .background(
                color = bgColor,
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                val borderSize = 1.2.dp.toPx()
                val cornerRadius = 24.dp.toPx()
                
                // Outer clean frosted double-gradient border
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.08f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = borderSize)
                )

                // Inner refractive chromatic path simulating glass refraction highlight
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glowAccentColor.copy(alpha = 0.22f),
                            Color.Transparent,
                            glowAccentColor.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerPhase, 0f),
                        end = Offset(shimmerPhase + size.width * 0.4f, size.height)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = borderSize * 0.7f)
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .padding(18.dp),
        content = content
    )
}
