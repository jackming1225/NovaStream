package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StreamDatabase
import com.example.data.StreamRepository
import com.example.decoder.HardwareCodecScanner
import com.example.decoder.VideoStreamingEngine
import com.example.model.CodecCapability
import com.example.model.PacingMode
import com.example.model.StreamConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StreamDatabase.getDatabase(application)
    private val repository = StreamRepository(database.streamDao())

    val engine = VideoStreamingEngine(viewModelScope)

    val streams: StateFlow<List<StreamConfig>> = repository.allStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCodecs: StateFlow<List<CodecCapability>> = MutableStateFlow(
        HardwareCodecScanner.scanCodecs()
    ).asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultPresets()
        }
    }

    fun selectStream(config: StreamConfig) {
        engine.setStreamConfig(config)
    }

    fun setPacingMode(mode: PacingMode) {
        engine.setPacingMode(mode)
    }

    fun setOptimization(enabled: Boolean) {
        engine.setOptimizationEnabled(enabled)
    }

    fun togglePlayback() {
        engine.togglePlayback()
    }

    fun saveCustomStream(config: StreamConfig) {
        viewModelScope.launch {
            repository.saveStream(config)
        }
    }

    fun deleteStream(config: StreamConfig) {
        viewModelScope.launch {
            repository.deleteStream(config)
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.stopPlayback()
    }
}
