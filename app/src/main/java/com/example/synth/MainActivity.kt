package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.synth.Constants.BUFFER_SIZE

class MainActivity : ComponentActivity() {
    private val signal = HarmonicSignal(Note.C_3)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        buffer = FloatArray(BUFFER_SIZE)
    )
    private val pianoViewModel = PianoViewModel(Note.toList(4))
    private val audioEngine = AudioEngine()

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.registerListener {
            viewModel.signal.value.evaluateToBuffer(
                viewModel.plotBuffer.value,
                false,
            )
        }
        audioEngine.signalBuffer.offer(setOf(viewModel.signal.value))

        setContent {
            Column {
                HarmonicSignalEditor(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    viewModel = viewModel
                )
                Piano(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = pianoViewModel
                )
            }
//        Piano(modifier = Modifier.fillMaxSize())
//            PianoKey(modifier = Modifier.fillMaxSize(), note = Note.A_4)

        }

    }

}
