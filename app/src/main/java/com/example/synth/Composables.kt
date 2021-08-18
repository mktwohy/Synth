package com.example.synth

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun log(text: String){ Log.d("m_tag",text) }

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


@Composable
fun HarmonicViewer(
    modifier: Modifier = Modifier,
    numSliders: Int,
    fundamental: Note = Note.A_4,
    signal: HarmonicSignal = HarmonicSignal(fundamental),
){
    var harmonicSeries = remember { mutableStateMapOf<Int,Float>() }

    var signal by remember { mutableStateOf(signal) }

    var signalData by remember {
        mutableStateOf(FloatArray(AudioEngine.BUFFER_SIZE))
    }

    fun updateSignal(){
        signal.updateHarmonicSeries(harmonicSeries)
        signal.evaluateToBuffer(signalData, true)
//        signalData = signal.evaluate(numPeriods, true)
    }

    Column(modifier) {
        RowOfVolumeSliders(
            modifier = Modifier.fillMaxHeight(0.50f),
            numSliders = numSliders,
            value = { sliderIndex -> harmonicSeries[sliderIndex+1] ?: 0f },
            onValueChange = { sliderIndex, value ->
                harmonicSeries[sliderIndex+1] = if(value < 0.01f) 0f else value
                updateSignal()
            }
        )
        XYPlot(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            data = signalData,
        )
    }
}

@Composable
fun RowOfVolumeSlidersScreen(
    modifier: Modifier = Modifier,
    numSliders: Int,
){
    var amplitudeState = remember { mutableStateMapOf<Int,Float>() }

    Column(modifier) {
        RowOfVolumeSliders(
            modifier = Modifier.fillMaxHeight(0.9f),
            numSliders = numSliders,
            value = { sliderIndex -> amplitudeState[sliderIndex] ?: 0f },
            onValueChange = { sliderIndex, value -> amplitudeState[sliderIndex] = value }
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
            for(sliderIndex in 0 until numSliders){
                Text(
                    text = ((amplitudeState[sliderIndex] ?: 0f) * 100).toInt().toString(),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RowOfVolumeSliders(
    modifier: Modifier = Modifier,
    numSliders: Int = 1,
    value: (Int) -> Float,
    onValueChange: (Int, Float) -> Unit
){
    BoxWithConstraints(modifier = modifier){
        val sliderWidth = this.maxWidth/numSliders
        val sliderHeight = this.maxHeight

        Row(modifier = Modifier) {
            for(sliderIndex in 0 until numSliders){
                VolumeSlider(
                    modifier = Modifier.size(sliderWidth, sliderHeight),
                    value = value(sliderIndex),
                    onValueChange = { onValueChange(sliderIndex, it) }
                )
            }
        }
    }
}

@Composable
fun VolumeSliderScreen(modifier: Modifier = Modifier, initialValue: Float = 0f){
    var amplitude by remember { mutableStateOf(initialValue) }

    VolumeSlider(
        modifier = modifier,
        value = amplitude,
        onValueChange = { amplitude = it }
    )
}

@Composable
fun VolumeSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
){
    Column(
        modifier = modifier.border(width = 1.dp, color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                modifier = Modifier
                    .requiredWidth(this.maxHeight)
                    .requiredHeight(this.maxWidth)
                    .rotate(-90f),
                value = value,
                onValueChange = onValueChange
            )
        }
        Text(
            text = (value * 100).toInt().toString(),
            color = Color.White
        )
    }
}