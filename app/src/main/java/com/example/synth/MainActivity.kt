package com.example.synth

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppModel{
    val audioEngine = AudioEngine()
    val noteRange = Note.C_3..Note.C_5
    val bendRange = -1f..1f
    val pianoViewModel = PianoViewModel()
    val oscillator = Oscillator(pianoViewModel.pressedNotes)

    val harmonicSeriesViewModel = HarmonicSeriesViewModel(oscillator.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(oscillator.harmonicSeries)
    val volumeSliderViewModel   = VolumeSliderViewModel(oscillator)
    val pitchBendViewModel      = PitchBendViewModel(oscillator)
}

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModel.audioEngine.start()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        audioEngine.registerListener {
//            harmonicSignalViewModel.signal.value.signals.forEach{
//                it.clock.save()
//                it.clock.angle = 0f
//            }
//            harmonicSignalViewModel.signal.value.evaluateToBuffer(harmonicSignalViewModel.plotBuffer.value)
//            harmonicSignalViewModel.signal.value.signals.forEach{ it.clock.restore()
//        }

//            audioEngine.signalBuffer.offer(
//                if(pianoViewModel.pressedNotes.isEmpty()) setOf(SilentSignal)
//                else pianoViewModel.pressedNotes.map { noteToSignal[it] ?: SilentSignal }.toSet()
//            )
//        }


        setContent {
            Column {
                HarmonicSeriesEditor(
                    modifier = Modifier.fillMaxHeight(0.25f),
                    viewModel = AppModel.harmonicSeriesViewModel
                )
                SignalPlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(Color.Black)
                        .border(1.dp, Color.White),
                    viewModel = AppModel.SignalPlotViewModel,
                    color = Color(0.4f, 0.0f, 1f, 1f),
                    strokeWidth = 5f

                )
                Row {
                    Piano(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.9f),
                        viewModel = AppModel.pianoViewModel
                    )
                    PitchBend(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color.White),
                        viewModel = AppModel.pitchBendViewModel
                    )
                }

            }
        }

    }

}
