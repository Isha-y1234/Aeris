package com.runanywhere.kotlin_starter_example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.kotlin_starter_example.data.SoundRepository
import com.runanywhere.kotlin_starter_example.services.HapticManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.runanywhere.kotlin_starter_example.data.SoundType

class MainViewModel : ViewModel() {

    val currentSound: StateFlow<SoundType?> = SoundRepository.currentSound

    // ✅ Real confidence exposed to UI
    val confidence: StateFlow<Int> = SoundRepository.confidence

    // ✅ Called once when toggle turns ON
    fun startDetectionListener(context: Context) {
        viewModelScope.launch {
            SoundRepository.currentSound.collectLatest { sound ->
                sound?.let {
                    HapticManager.trigger(context, it)
                }
            }
        }
    }
}