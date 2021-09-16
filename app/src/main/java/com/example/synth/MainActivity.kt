package com.example.synth

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.synth.Note.Companion.plus

object AppModel{
    var noteRange = Note.C_3..Note.C_5
        set(value){
            field = value
            logd(value)
            pianoViewModel.pianoGrid.noteRange = value
            oscillator.assignSignalsToNotes()
        }
    val audioEngine = AudioEngine()
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
        AppModel.oscillator.harmonicSeries[1] = 1f
        setContent {
            val isPortrait = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            Column {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .fillMaxHeight(0.05f),
//                    horizontalArrangement = Arrangement.Start,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        modifier = Modifier.fillMaxWidth(0.1f),
//                        text = "Latency: ${AppModel.lag}",
//                        color = Color.White
//                    )
//                    LinearProgressIndicator(
//                        modifier = Modifier
//                            .fillMaxHeight(0.5f)
//                            .fillMaxWidth(0.5f)
//                            .border(1.dp, Color.White),
//                        progress = AppModel.lag/Constants.BUFFER_TIME_MS,
//                        color = run{
//                            var clippingAmount = (AppModel.lag / Constants.BUFFER_TIME_MS)
//                            if(clippingAmount > 1) clippingAmount = 1f
//                            Color(
//                                red = clippingAmount,
//                                green = 1f - clippingAmount,
//                                blue = 0f
//                            )
//                        }
//                    )
//                }

                HarmonicSeriesEditor(
                    modifier = Modifier
                        .fillMaxHeight(if (isPortrait) 0.5f else 0.3f)
                        .border(1.dp, Color.White),
                    viewModel = AppModel.harmonicSeriesViewModel
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .border(1.dp, Color.White)
                ) {
                    SignalPlot(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.9f)
                            .background(Color.Black)
                            .border(1.dp, Color.White),
                        viewModel = AppModel.SignalPlotViewModel,
                        color = Color(0.4f, 0.0f, 1f, 1f),
                        strokeWidth = 5f
                    )
                    WaveShapeSelector(
                        modifier = Modifier
                            .fillMaxSize(),
                        viewModel = AppModel.waveFormChangeViewModel
                    )
                }

                Row(Modifier.border(1.dp, Color.White)) {
                    Piano(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (isPortrait) 0.9f else 0.95f),
                        viewModel = AppModel.pianoViewModel
                    )
                    Button(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                        onClick = {
                            val newStart = AppModel.noteRange.start
                            val newEnd = AppModel.noteRange.endInclusive+1
                            AppModel.noteRange = newStart..newEnd
                        }
                    ){}
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
