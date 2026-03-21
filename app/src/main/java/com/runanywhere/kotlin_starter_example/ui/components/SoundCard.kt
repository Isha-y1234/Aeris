package com.runanywhere.kotlin_starter_example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runanywhere.kotlin_starter_example.data.SoundType

@Composable
fun SoundCard(sound: SoundType, confidence: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = sound.name, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(progress = confidence)

            Spacer(modifier = Modifier.height(4.dp))

            Text("${(confidence * 100).toInt()}% confidence")
        }
    }
}