package com.example.synth

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

object AppModel{
    val audioEngine = AudioEngine()
    val noteRange = Note.C_3..Note.C_5
    val bendRange = -1f..1f
    val oscillator = Oscillator()

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(oscillator.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(oscillator.harmonicSeries)
    val waveFormChangeViewModel = WaveFormChangeViewModel(oscillator)
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

        AppModel.oscillator.harmonicSeries[1] = 1f
        setContent {
            val isPortrait = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            Column {
                WaveShapeSelector(
                    modifier = Modifier
                        .fillMaxHeight(if(isPortrait) 0.05f else 0.1f)
                        .fillMaxWidth(),
                    viewModel = AppModel.waveFormChangeViewModel
                )
                HarmonicSeriesEditor(
                    modifier = Modifier.fillMaxHeight(if(isPortrait) 0.5f else 0.3f),
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
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (isPortrait) 0.9f else 0.95f),
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
