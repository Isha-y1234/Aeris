package com.runanywhere.kotlin_starter_example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.runanywhere.kotlin_starter_example.data.SoundType

@Composable
fun SensitivitySlider(
    sound: SoundType,
    value: Float,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = sound.name)

        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..1f
        )
    }
}