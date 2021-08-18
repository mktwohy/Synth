package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
    private val signal = HarmonicSignal(Note.B_1)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        buffer = FloatArray(AudioEngine.BUFFER_SIZE)
    )
    private val audioEngine = AudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.masterSignal.signals.add(signal)
        audioEngine.registerListener { viewModel.numBuffersPlayed.value++ }
        audioEngine.registerListener { viewModel.buffer.value = it }

        setContent {
            HarmonicSignalEditor(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
//            HarmonicSeriesScreen(
//                modifier = Modifier.fillMaxSize()
//            )
        }

    }

}
