package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
//    private val audioEngine = AudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        audioEngine.start()
        setContent {
            HarmonicViewer(
                modifier = Modifier.fillMaxSize(),
                numSliders = 10,
//                audioEngine = audioEngine
            )
        }

    }
}
