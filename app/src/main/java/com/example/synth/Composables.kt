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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

//@Composable
//fun HarmonicSeriesScreen(
//    modifier: Modifier = Modifier,
//){
//    val harmonicSeries by remember { mutableStateOf(HarmonicSeries()) }
//    log("compose!")
//    RowOfVolumeSliders(
//        modifier = modifier.fillMaxHeight(0.50f),
//        numSliders = Constants.NUM_HARMONICS,
//        value = { sliderIndex -> harmonicSeries[sliderIndex+1]},
//        onValueChange = { sliderIndex, value ->
//            harmonicSeries[sliderIndex+1] = if(value < 0.01f) 0f else value
//        }
//    )
//}

class HarmonicSignalViewModel(
    signal: HarmonicSignal,
    buffer: FloatArray
) : ViewModel(){
    val signal: MutableState<HarmonicSignal> = mutableStateOf(signal)
    val signalCopy: MutableState<HarmonicSignal> = mutableStateOf(
        HarmonicSignal(signal.fundamental, signal.harmonicSeries)
    )
    var buffer: MutableState<FloatArray> = mutableStateOf(buffer)
    var bufferCopy: MutableState<FloatArray> = mutableStateOf(buffer)
    val numBuffersPlayed: MutableState<Int> = mutableStateOf(0)
}

@Composable
fun HarmonicSignalEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    val sliderState = remember {
        mutableStateListOf<Float>().apply {
            repeat(Constants.NUM_HARMONICS){ this.add(0f) }
        }
    }

    Column(modifier) {
//        Text(text = viewModel.numBuffersPlayed.value.toString(), color = Color.White)
        RowOfVolumeSliders(
            modifier = Modifier.fillMaxHeight(0.50f),
            numSliders = Constants.NUM_HARMONICS,
            value = { sliderIndex -> sliderState[sliderIndex] },
            onValueChange = { sliderIndex, sliderValue ->
                val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue
                sliderState[sliderIndex] = newSliderValue
                viewModel.signal.value.harmonicSeries[sliderIndex+1] = newSliderValue
            }
        )
        Row {
            XYPlot(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .background(Color.Black),
                color = Color.White,
                data = viewModel.bufferCopy.value,
            )
            Column {
                VolumeSliderScreen(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth(),
                    initialValue = viewModel.signal.value.amp,
                    onValueChange = {
                        viewModel.signal.value.amp = it
                        viewModel.signalCopy.value.amp = it
                    }
                )
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        sliderState.indices.forEach { sliderState[it] = 0f }
                        viewModel.signal.value.harmonicSeries.reset()
                    }
                ) { Text("Reset") }
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
fun RowOfVolumeSlidersScreen(
    modifier: Modifier = Modifier,
    amplitudes: List<Float>,
    numSliders: Int,
){
    var amplitudeState = remember { mutableStateMapOf<Int,Float>() }

    RowOfVolumeSliders(
        modifier = Modifier.fillMaxHeight(0.9f),
        numSliders = numSliders,
        value = { sliderIndex -> amplitudeState[sliderIndex] ?: 0f },
        onValueChange = { sliderIndex, value -> amplitudeState[sliderIndex] = value }
    )

}

@Composable
fun VolumeSliderScreen(
    modifier: Modifier = Modifier,
    initialValue: Float = 0f,
    onValueChange: (Float) -> Unit
){
    var amplitude by remember { mutableStateOf(initialValue) }

    VolumeSlider(
        modifier = modifier,
        value = amplitude,
        onValueChange = {
            amplitude = it
            onValueChange(it)
        }
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