package com.example.synth

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.signallib.Note.Companion.minus
import com.example.signallib.Note.Companion.plus


@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (AppModel.startup) startup()

        setContent {
            val isPortrait = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            Column {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.1f)){
                    Button(
                        onClick = { AppModel.pianoViewModel.shiftNoteRange(-1, 0) }
                    ) {
                        Text("<", color = Color.White)
                    }
                    Button(
                        onClick = { AppModel.pianoViewModel.shiftNoteRange(1, 0) }
                    ) {
                        Text(">", color = Color.White)
                    }
                    Button(
                        onClick = { AppModel.pianoViewModel.shiftNoteRange(0, -1) }
                    ) {
                        Text("<", color = Color.White)
                    }
                    Button(
                        onClick = { AppModel.pianoViewModel.shiftNoteRange(0, 1) }

                    ) {
                        Text(">", color = Color.White)
                    }
                }
                XYPlot(
                    data = AppModel.currentAudio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.2f)
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
                        viewModel = AppModel.pianoViewModel,
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

    private fun startup(){
        val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        with(AppModel){
            // get phone's sample rate and buffer size
            signalSettings.sampleRate = am.getProperty(
                AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
            ).toInt()

            signalSettings.bufferSize = am.getProperty(
                AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
            ).toInt()

            // reset the signalEngine so that it's AudioTrack uses this up-to-date
            signalEngine.reset()
            signalEngine.play()
            signalEngine.registerOnBufferUpdateCallback {
                currentAudio = it.toList()
            }
            signalSettings.harmonicSeries[1] = 1f

            // ensure that this doesn't run when screen rotates
            startup = false
        }
    }

    override fun onPause() {
        super.onPause()
        AppModel.signalEngine.pause()
    }

    override fun onResume() {
        super.onResume()
        AppModel.signalEngine.play()
    }
}
