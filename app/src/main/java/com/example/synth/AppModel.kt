package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.*

object AppModel{
    const val NUM_HARMONICS    = 15
    const val SAMPLE_RATE      = 44800
    const val BUFFER_SIZE      = 192

    val signalManager = SignalManager(
        sampleRate = SAMPLE_RATE,
        waveShape = WaveShape.SINE,
        harmonicSeries = HarmonicSeries(NUM_HARMONICS)
    )
    val signalEngine  = SignalEngine(SAMPLE_RATE, BUFFER_SIZE, signalManager)

    var noteRange = Note.C_3..Note.C_5
    var pitchBend = 0f
        set(value){
            signalEngine.updatePitchBend(value)
            field = value
        }
    var currentAudio by mutableStateOf<List<Float>>(listOf())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalManager.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(
        HarmonicSignal(
            sampleRate = SAMPLE_RATE,
            fundamental = Note.A_4,
            harmonicSeries = signalManager.harmonicSeries
        )
    )
    val waveFormChangeViewModel = WaveFormChangeViewModel(signalManager)
    val volumeSliderViewModel   = VolumeSliderViewModel(signalManager)
    val pitchBendViewModel      = PitchBendViewModel(signalManager)
}