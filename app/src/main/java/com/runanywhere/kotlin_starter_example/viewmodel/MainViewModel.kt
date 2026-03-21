package com.runanywhere.kotlin_starter_example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.kotlin_starter_example.data.SoundRepository
import com.runanywhere.kotlin_starter_example.data.SoundType
import com.runanywhere.kotlin_starter_example.services.HapticManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // 🔊 Current detected sound (from repository)
    val currentSound: StateFlow<SoundType?> =
        SoundRepository.currentSound
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // 🎯 Optional: UI-friendly text
    val statusText: StateFlow<String> =
        currentSound
            .map { sound ->
                sound?.name ?: "Listening..."
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Listening..."
            )

    // 📳 Trigger haptics when sound detected
    fun handleSound(context: Context, sound: SoundType) {
        HapticManager.trigger(context, sound)
    }

    // 🔥 Auto-listen to changes (optional advanced)
    fun startObserving(context: Context) {
        viewModelScope.launch {
            currentSound.collect { sound ->
                sound?.let {
                    HapticManager.trigger(context, it)
                }
            }
        }
    }
}