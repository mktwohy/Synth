package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    private val signal = HarmonicSignal(Note.B_1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.masterSignal.signals.add(signal)
        setContent {
            HarmonicSignalEditor(
                modifier = Modifier.fillMaxSize(),
                signal = signal,
                audioEngine = audioEngine
            )
//            HarmonicSeriesScreen(
//                modifier = Modifier.fillMaxSize()
//            )
        }

    }

}
