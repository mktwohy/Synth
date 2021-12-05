package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.*

object AppModel{
    const val NUM_HARMONICS    = 15
    const val SAMPLE_RATE      = 44800
    const val BUFFER_SIZE      = 192

    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(NUM_HARMONICS),
        waveShape = WaveShape.SINE,
        sampleRate = SAMPLE_RATE,
        bufferSize = BUFFER_SIZE
    )

    val signalManager = SignalManager(signalSettings)
    val signalEngine  = SignalEngine(SAMPLE_RATE, BUFFER_SIZE, signalManager)

    var noteRange = Note.C_3..Note.C_5
    var pitchBend = 0f
        set(value){
            signalEngine.updatePitchBend(value)
            field = value
        }
    var currentAudio by mutableStateOf<List<Float>>(listOf())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalSettings.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(
        HarmonicSignal(
            sampleRate = SAMPLE_RATE,
            fundamental = Note.A_4,
            harmonicSeries = signalSettings.harmonicSeries
        )
    )
    val waveFormChangeViewModel = WaveFormChangeViewModel(signalManager)
    val volumeSliderViewModel   = VolumeSliderViewModel(signalManager)
    val pitchBendViewModel      = PitchBendViewModel(signalManager)
}