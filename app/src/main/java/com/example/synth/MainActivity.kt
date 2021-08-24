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
    val plotSignal = HarmonicSignal(Note.C_3)
    private val viewModel = HarmonicSignalViewModel(
        signal = plotSignal,
        plotBuffer = FloatArray(plotSignal.period.toInt()*4)
    )
    private val pianoViewModel = PianoViewModel(Note.toList(4))
    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>().apply {
        pianoViewModel.notes.forEach{
            this[it] = HarmonicSignal(it, plotSignal.harmonicSeries)
        }
    }


    @ExperimentalComposeUiApi
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

            audioEngine.signalBuffer.offer(
                if(pianoViewModel.pressedNotes.isEmpty()) setOf(SilentSignal)
                else pianoViewModel.pressedNotes.map { noteToSignal[it]!! }.toSet()
            )
        }


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
