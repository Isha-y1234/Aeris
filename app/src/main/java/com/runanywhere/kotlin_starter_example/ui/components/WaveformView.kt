package com.runanywhere.kotlin_starter_example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.random.Random

@Composable
fun WaveformView() {

    val bars = remember { List(20) { Random.nextFloat() } }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val barWidth = size.width / bars.size

        bars.forEachIndexed { index, value ->
            drawRect(
                color = Color.Blue, // ✅ FIXED
                topLeft = Offset(
                    x = index * barWidth,
                    y = size.height * (1 - value)
                ),
                size = Size(
                    barWidth * 0.8f,
                    size.height * value
                )
            )
        }
    }
}