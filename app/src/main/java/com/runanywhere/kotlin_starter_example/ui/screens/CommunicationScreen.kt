package com.runanywhere.kotlin_starter_example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.runanywhere.kotlin_starter_example.data.SettingsRepository

@Composable
fun CommunicationScreen(
    onConverse: () -> Unit,
    onVoiceProxy: () -> Unit,
    onCaptions: () -> Unit
) {
    val walkthroughCompleted by SettingsRepository.commWalkthroughCompleted.collectAsState()
    var showWalkthrough by remember { mutableStateOf(false) }

    LaunchedEffect(walkthroughCompleted) {
        if (!walkthroughCompleted) {
            showWalkthrough = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF6FB1FC), Color(0xFFA7C6FF))))
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Text(
                    text = "Communication",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CommunicationToolCard(
                    title = "Converse",
                    description = "Two-way speech translation with AI suggestions",
                    icon = Icons.Default.Forum,
                    color = Color(0xFF9C6FFC),
                    onClick = onConverse
                )

                CommunicationToolCard(
                    title = "Voice Proxy",
                    description = "Type and have the AI speak for you",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    color = Color(0xFF6FB1FC),
                    onClick = onVoiceProxy
                )

                CommunicationToolCard(
                    title = "Live Captions",
                    description = "Real-time transcription of environmental speech",
                    icon = Icons.Default.ClosedCaption,
                    color = Color(0xFF6BCB77),
                    onClick = onCaptions
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // ── Communication Walkthrough Overlay ─────────────────
        AnimatedVisibility(
            visible = showWalkthrough,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.zIndex(100f)
        ) {
            CommWalkthroughOverlay(
                onDismiss = {
                    showWalkthrough = false
                    SettingsRepository.setCommWalkthroughCompleted(true)
                }
            )
        }
    }
}

@Composable
private fun CommWalkthroughOverlay(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comm Tools ($step/$totalSteps)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6FB1FC)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Skip", color = Color(0xFFB0B0B0))
                    }
                }

                Spacer(Modifier.height(20.dp))

                val (icon, title, desc) = when (step) {
                    1 -> Triple(
                        Icons.Default.Forum,
                        "Converse",
                        "Best for face-to-face talks. It listens, transcribes, and offers smart AI replies."
                    )
                    2 -> Triple(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        "Voice Proxy",
                        "Type anything and the phone will speak it aloud in a natural voice."
                    )
                    else -> Triple(
                        Icons.Default.ClosedCaption,
                        "Live Captions",
                        "Great for lectures or meetings. It transcribes continuous speech into text."
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6FB1FC).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color(0xFF6FB1FC), modifier = Modifier.size(40.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2340)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = desc,
                    fontSize = 15.sp,
                    color = Color(0xFF6B7A9A),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (step < totalSteps) step++ else onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FB1FC))
                ) {
                    Text(if (step < totalSteps) "Next" else "Got it!")
                }
            }
        }
    }
}

@Composable
private fun CommunicationToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2340)
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7A9A)
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFB0B0B0))
        }
    }
}
