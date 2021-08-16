package com.example.synth

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

fun log(text: String){ Log.d("m_tag",text) }

val buffer = FloatArray(AudioEngine.BUFFER_SIZE)

@Composable
fun XYPlotScreen(){
    var data by remember {
        mutableStateOf(FloatArray(AudioEngine.BUFFER_SIZE))
    }
    var signal: Signal by remember { mutableStateOf(PeriodicSignal()) }

    fun generateRandomSignal(){
        signal = SumSignal(
            Signal.signalsFromHarmonicSeries(
                Signal.harmonicSeries(
                    1,
                    15,
                    0.5f,
                    0.1f
                ),
                Note.A_4
            )
        )
    }

    fun evaluateBuffer(){
        signal.evaluateTo(buffer,false)
        data = buffer
    }

    Column {
        XYPlot(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.80f)
                .background(Color.White),
            data = data,
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement =  Arrangement.SpaceEvenly)
        {
            Button(
                onClick = { evaluateBuffer() }
            ){ Text("Update Signal") }

            Button(
                onClick = {
                    generateRandomSignal()
                    evaluateBuffer()
                }
            ){ Text("Generate New Signal") }
        }
    }


}



@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    data: FloatArray,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    Canvas(modifier = modifier) {
        for(i in 0..data.size-2){
            drawLine(
                start = Offset(
                    x = i * size.width / (data.size-1),
                    y = (data[i] * size.height/2) + (size.height/2)
                ),
                end = Offset(
                    x = (i+1) * size.width / (data.size-1),
                    y = (data[i+1] * size.height/2) + (size.height/2)
                ),
                color = color,
                strokeWidth = strokeWidth
            )
        }
    }
}
