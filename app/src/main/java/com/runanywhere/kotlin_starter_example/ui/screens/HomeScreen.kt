package com.runanywhere.kotlin_starter_example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.kotlin_starter_example.data.SoundType
import com.runanywhere.kotlin_starter_example.services.AudioForegroundService
import com.runanywhere.kotlin_starter_example.services.ModelService
import com.runanywhere.kotlin_starter_example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onLive: () -> Unit,
    onSettings: () -> Unit,
    onHaptics: () -> Unit
) {
    val context = LocalContext.current
    val modelService: ModelService = viewModel()

    val sound: SoundType? by viewModel.currentSound.collectAsState(initial = null)
    var isOn by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOn) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val softBlueStart = Color(0xFF6FB1FC)
    val softBlueEnd   = Color(0xFFA7C6FF)
    val alertRed      = Color(0xFFFF6B6B)
    val currentSound  = sound

    val statusColor = when {
        currentSound != null && isOn -> alertRed
        isOn                         -> softBlueStart
        else                         -> Color(0xFFB0B0B0)
    }

    val statusText = when {
        !isOn                -> "System Off"
        currentSound != null -> "${currentSound.name} Detected"
        else                 -> "Listening…"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(softBlueStart, softBlueEnd)))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Text(
                        text = "Aeris",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Stay aware without looking",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Toggle ───────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .shadow(
                        elevation = if (isOn) 20.dp else 6.dp,
                        shape = CircleShape,
                        ambientColor = softBlueStart.copy(alpha = 0.4f),
                        spotColor = softBlueStart.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (isOn)
                            Brush.radialGradient(listOf(softBlueStart, softBlueEnd))
                        else
                            Brush.radialGradient(
                                listOf(Color(0xFFDDE3EE), Color(0xFFB8C0D0))
                            )
                    )
                    .clickable {
                        isOn = !isOn
                        if (isOn) {
                            context.startForegroundService(
                                Intent(context, AudioForegroundService::class.java)
                            )
                            viewModel.startDetectionListener(context)
                        } else {
                            context.stopService(
                                Intent(context, AudioForegroundService::class.java)
                            )
                        }
                    }
            ) {
                Icon(
                    imageVector = if (isOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Status Card ──────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                !isOn                -> Icons.Default.MicOff
                                currentSound != null -> Icons.Default.Warning
                                else                 -> Icons.Default.GraphicEq
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = statusText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A2340)
                        )
                        Text(
                            text = if (isOn) "System active" else "Tap toggle to start",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7A9A)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Quick Actions ────────────────────────────────────
            Text(
                text = "Quick Actions",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7A9A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.GraphicEq,
                    label = "Live",
                    modifier = Modifier.weight(1f),
                    onClick = onLive
                )
                QuickActionCard(
                    icon = Icons.Default.Tune,
                    label = "Sensitivity",
                    modifier = Modifier.weight(1f),
                    onClick = onSettings
                )
                QuickActionCard(
                    icon = Icons.Default.Vibration,
                    label = "Haptics",
                    modifier = Modifier.weight(1f),
                    onClick = onHaptics
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── STT Model Card ───────────────────────────────────
            STTModelCard(modelService = modelService)
        }
    }
}

@Composable
private fun STTModelCard(modelService: ModelService) {
    val softBlueStart = Color(0xFF6FB1FC)
    val softBlueEnd   = Color(0xFFA7C6FF)
    val green         = Color(0xFF6BCB77)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (modelService.isSTTLoaded)
                                green.copy(alpha = 0.12f)
                            else
                                softBlueStart.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (modelService.isSTTLoaded)
                            Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = if (modelService.isSTTLoaded) green else softBlueStart,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sound Detection Model",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A2340)
                    )
                    Text(
                        text = when {
                            modelService.isSTTLoaded     -> "Ready — detection active"
                            modelService.isSTTLoading    -> "Loading model…"
                            modelService.isSTTDownloading -> "Downloading…"
                            else                         -> "Required for sound detection"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF6B7A9A)
                    )
                }
            }

            // Progress bar while downloading
            if (modelService.isSTTDownloading) {
                Spacer(Modifier.height(14.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading Whisper Tiny",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7A9A)
                        )
                        Text(
                            text = "${(modelService.sttDownloadProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = softBlueStart
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { modelService.sttDownloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = softBlueStart,
                        trackColor = Color(0xFFEEF0F5)
                    )
                }
            }

            // Loading spinner
            if (modelService.isSTTLoading) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = softBlueStart,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Loading into memory…",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7A9A)
                    )
                }
            }

            // Download button — only show if not loaded/downloading/loading
            if (!modelService.isSTTLoaded &&
                !modelService.isSTTDownloading &&
                !modelService.isSTTLoading
            ) {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { modelService.downloadAndLoadSTT() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = softBlueStart
                    )
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Download Detection Model",
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "~40MB • Required for keyword detection",
                    fontSize = 11.sp,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Ready state with gradient indicator
            if (modelService.isSTTLoaded) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(green.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(green)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Say 'siren', 'alarm', 'hello' to test",
                            fontSize = 12.sp,
                            color = green
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF6FB1FC),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A2340)
            )
        }
    }
}