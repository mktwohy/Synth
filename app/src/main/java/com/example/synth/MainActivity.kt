package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.synth.Constants.BUFFER_SIZE
import com.example.synth.Note.Companion.toList

object AppModel{
    val noteRange = Note.C_3..Note.C_4
    val oscillator = Oscillator()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(oscillator.harmonicSeries)
    val pianoViewModel = PianoViewModel(noteRange)
    val volumeSliderViewModel = VolumeSliderViewModel(oscillator)
}


class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    val plotSignal = HarmonicSignal(Note.C_3)
    private val viewModel = HarmonicSignalViewModel(
        signal = plotSignal,
        plotBuffer = FloatArray(plotSignal.period.toInt()*4)
    )
    private val pianoViewModel = PianoViewModel(Note.C_3..Note.C_4)
    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>().apply {
        AppModel.noteRange.toList().forEach{
            this[it] = HarmonicSignal(it, plotSignal.harmonicSeries, 1/7f)
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
                else pianoViewModel.pressedNotes.map { noteToSignal[it] ?: SilentSignal }.toSet()
            )
        }


        setContent {
            Column {
                HarmonicSeriesEditor(
                    modifier = Modifier.fillMaxHeight(0.25f),
                    viewModel = AppModel.harmonicSeriesViewModel
                )
                Row(Modifier.fillMaxHeight(0.5f).border(1.dp, Color.White)) {
                    XYPlot(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.8f)
                            .background(Color.Black)
                            .border(1.dp, Color.White),
                        color = Color(0.4f, 0.0f, 1f, 1f),
                        strokeWidth = 5f,
                        data = viewModel.plotBuffer.value,
                    )
                    PitchBend(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f),
                        viewModel = viewModel
                    )
                    VerticalSlider(
                        modifier = Modifier.fillMaxSize(),
                        value = AppModel.volumeSliderViewModel.sliderState,
                        onValueChange = {
                            AppModel.oscillator.amplitude = volumeToAmplitude(it)
                        }
                    )
                }
                Piano(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = pianoViewModel
                )
            }
        }

    }

}
