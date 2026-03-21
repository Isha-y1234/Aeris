package com.runanywhere.kotlin_starter_example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ToggleButton(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = { onToggle(!isOn) }, modifier = modifier) {
        Text(if (isOn) "ON" else "OFF")
    }
}