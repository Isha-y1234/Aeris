package com.runanywhere.kotlin_starter_example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.kotlin_starter_example.data.*
import com.runanywhere.kotlin_starter_example.services.HapticManager
import com.runanywhere.kotlin_starter_example.services.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val uri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}

class MainViewModel : ViewModel() {

    val currentSound: StateFlow<SoundType?> = SoundRepository.currentSound
    val confidence: StateFlow<Int> = SoundRepository.confidence
    val history: StateFlow<List<SoundEvent>> = SoundRepository.history

    private val _exportState = MutableSharedFlow<ExportState>()
    val exportState: SharedFlow<ExportState> = _exportState

    fun startDetectionListener(context: Context) {
        viewModelScope.launch {
            SoundRepository.currentSound.collectLatest { sound ->
                sound?.let { HapticManager.trigger(context, it) }
            }
        }
    }

    fun exportCaptions(context: Context, captions: List<CaptionLine>) {
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            _exportState.emit(ExportState.Loading)
            try {
                if (captions.isEmpty()) {
                    _exportState.emit(ExportState.Error("No captions to export"))
                    return@launch
                }

                val export = CaptionsExport(captions)
                val uri = PdfExporter.export(appContext, export)

                if (uri != null) {
                    _exportState.emit(ExportState.Success(uri))
                } else {
                    _exportState.emit(ExportState.Error("Failed to create PDF"))
                }
            } catch (e: Exception) {
                _exportState.emit(ExportState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
