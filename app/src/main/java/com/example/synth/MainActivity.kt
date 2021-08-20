package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.example.synth.Constants.BUFFER_SIZE

class MainActivity : ComponentActivity() {
    private val signal = HarmonicSignal(Note.C_2)
    private val viewModel = HarmonicSignalViewModel(
        signal = signal,
        buffer = FloatArray(BUFFER_SIZE)
    )
    private val pianoViewModel = PianoViewModel(Note.toList(4))
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
//        Piano(modifier = Modifier.fillMaxSize())
//            PianoKey(modifier = Modifier.fillMaxSize(), note = Note.A_4)
            Piano(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = pianoViewModel
            )
        }

    }

}
