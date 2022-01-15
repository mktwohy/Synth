package com.example.synth

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalConfiguration
import com.example.synth.ui.composables.Main
import com.example.synth.ui.composables.TestScalingSlider

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (AppModel.startup) startup()

        setContent {
            val isPortrait = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            Main(isPortrait)
//            TestScalingSlider()
        }
    }


    private fun startup(){
        // ensure that this only runs once.
        // if this variable isn't used, it will run every time screen rotates
        AppModel.startup = false

        val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        with(AppModel){
            // get phone's sample rate and buffer size
            signalSettings.sampleRate = am.getProperty(
                AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
            ).toInt()

            signalSettings.bufferSize = am.getProperty(
                AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
            ).toInt()

            // reset the signalEngine so that its AudioTrack uses this up-to-date
            signalEngine.reset()
            signalEngine.registerAfterBufferWriteCallback {
                currentAudio = it.toList()
            }

            signalEngine.play()
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
