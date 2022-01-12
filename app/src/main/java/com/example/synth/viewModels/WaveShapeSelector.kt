package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.enums.WaveShape

class WaveShapeSelectorViewModel(val signalSettings: SignalSettings): ViewModel() {
    var waveShapeName by mutableStateOf(signalSettings.waveShape.abbreviation)
    private var index = 0
    private val waveShapes = WaveShape.values()
    fun nextWaveShape(){
        index = (index + 1) % waveShapes.size
        signalSettings.waveShape = waveShapes[index]
    }

    init {
        signalSettings.registerWaveShapeListener {
            waveShapeName = it.abbreviation
        }
    }
}