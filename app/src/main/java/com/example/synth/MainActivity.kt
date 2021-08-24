package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.example.synth.Constants.BUFFER_SIZE

class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    private val signal = HarmonicSignal(Note.C_3)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        plotBuffer = FloatArray(signal.period.toInt()*4)
    )
    private val pianoViewModel = PianoViewModel(Note.toList(4))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.registerListener {
            viewModel.signal.value.signals.forEach{
                it.clock.save()
                it.clock.angle = 0f
            }
            viewModel.signal.value.evaluateToBuffer(viewModel.plotBuffer.value)
            viewModel.signal.value.signals.forEach{ it.clock.restore() }
        }
        audioEngine.signalBuffer.offer(setOf(viewModel.signal.value))

        setContent {
            Column {
                Main(
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
