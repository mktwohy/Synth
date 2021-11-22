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


@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)


        with(AppModel){
            signalEngine.start()
            signalEngine.registerOnBufferUpdateCallback {
                currentAudio = it.toList()
            }
            signalManager.harmonicSeries[1] = 1f
        }

        setContent {
            val isPortrait = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            Column {
                XYPlot(
                    data = AppModel.currentAudio,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.25f),
                )
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
                    PitchBend(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color.White),
                        //viewModel = AppModel.pitchBendViewModel
                    )
                }

            }
        }
    }
}
