package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.synth.Constants.BUFFER_SIZE


class MainActivity : ComponentActivity() {
    private val signal = HarmonicSignal(Note.A_3)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        buffer = FloatArray(BUFFER_SIZE)
    )
    private val audioEngine = AudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.masterSignal.signals.add(signal)
        audioEngine.registerListener {
            viewModel.soundBuffer.value = it
            viewModel.signal.value.evaluateToBuffer(
                viewModel.plotBuffer.value,
                false,
                isolated = true
            )
        }

        setContent {
            HarmonicSignalEditor(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )

        }

    }

}
