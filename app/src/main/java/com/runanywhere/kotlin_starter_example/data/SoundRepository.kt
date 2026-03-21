package com.runanywhere.kotlin_starter_example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SoundRepository {

    private val _currentSound = MutableStateFlow<SoundType?>(null)
    val currentSound: StateFlow<SoundType?> = _currentSound

    private val _confidence = MutableStateFlow(0)
    val confidence: StateFlow<Int> = _confidence

    // ✅ Called by AudioForegroundService with full predictions map
    fun updateDetection(predictions: Map<SoundType, Float>) {
        if (predictions.isEmpty()) {
            _currentSound.value = null
            _confidence.value = 0
            return
        }
        val top = predictions.maxByOrNull { it.value }!!
        _currentSound.value = top.key
        _confidence.value = (top.value * 100).toInt()
    }

    // ✅ Also keep update() so old SoundClassifier code doesn't break
    fun update(sound: SoundType?, confidenceScore: Float) {
        _currentSound.value = sound
        _confidence.value = (confidenceScore * 100).toInt()
    }
}