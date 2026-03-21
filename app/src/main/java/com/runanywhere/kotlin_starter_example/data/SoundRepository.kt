package com.runanywhere.kotlin_starter_example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SoundRepository {

    private val _currentSound = MutableStateFlow<SoundType?>(null)
    val currentSound: StateFlow<SoundType?> = _currentSound

    fun updateDetection(predictions: Map<SoundType, Float>) {
        val best = predictions.maxByOrNull { it.value }?.key
        _currentSound.value = best
    }
}