package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.synth.Constants.BUFFER_SIZE

class MainActivity : ComponentActivity() {
    private val signal = HarmonicSignal(Note.C_2)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        buffer = FloatArray(BUFFER_SIZE)
    )
    private val audioEngine = AudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine.start()
        audioEngine.registerListener {
            viewModel.signal.value.evaluateToBuffer(
                viewModel.plotBuffer.value,
                false,
            )
        }

        setContent {
//            Column {
//                HarmonicSignalEditor(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .fillMaxHeight(0.8f),
//                    viewModel = viewModel
//                )
//                BoxWithConstraints {
//                    val width = this.maxWidth
//                    val height = this.maxHeight
//                    Row {
//                        repeat(7){
//                            Button(
//                                modifier = Modifier
//                                    .size(width/7, height)
//                                    .background(color = Color.White)
//                                    .border(width = 1.dp, color = Color.Black)
//                                ,
//                                onClick = { audioEngine.signalBuffer.offer(setOf(viewModel.signal.value)) }
//                            ) {
//
//                            }
//                        }
//                    }
//                }
//            }
        Piano(modifier = Modifier.fillMaxSize())

        }

    }

}
