package com.example.synth

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlin.math.pow

fun log(text: String){ Log.d("m_tag",text) }

class PianoViewModel(): ViewModel(){
    var notes = mutableStateListOf(Note.toList(4))

}

fun getTopWidths(chunkIndex: Int) =
    when(chunkIndex){
        0 -> listOf(3/28f to White, 1/28f to Black)
        1 -> listOf(1/28f to Black, 1/14f to White, 1/28f to Black)
        2 -> listOf(1/28f to Black, 3/28f to White)
        3 -> listOf(3/28f to White, 1/28f to Black)
        4 -> listOf(1/28f to Black, 1/14f to White, 1/28f to Black)
        5 -> listOf(1/28f to Black, 1/14f to White, 1/28f to Black)
        6 -> listOf(1/28f to Black, 3/28f to White)
        else -> listOf()
    }



@Composable
fun Piano(
    modifier: Modifier,
){
    BoxWithConstraints(modifier.background(Color.Green)) {
        val height = this.maxHeight
        val width = this.maxWidth
        Column{
            Row(
                modifier = Modifier.fillMaxHeight(0.5f),
                horizontalArrangement = Arrangement.Center
            ) {
                for(i in 0..6){
                    for((widthMultiplier, color) in getTopWidths(i)){
                        Box(modifier = Modifier
                                .size(width * widthMultiplier, height)
                                .background(color)
                                .border(0.5.dp, Black)
                        )
                    }
                }
            }
            Row{
                repeat(7){
                    Box(modifier = Modifier
                            .size(width * 1/7f, height)
                            .background(White)
                            .border(0.5.dp, Black)
                    )
                }
            }
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

class HarmonicSignalViewModel(
    signal: HarmonicSignal,
    buffer: FloatArray
) : ViewModel(){
    val signal: MutableState<HarmonicSignal> = mutableStateOf(signal)
    var plotBuffer: MutableState<FloatArray> = mutableStateOf(buffer.copyOf())
    var bendAmount: MutableState<Float> = mutableStateOf(1f)
    var volume: MutableState<Float> = mutableStateOf(1f)
    var harmonicSliders = mutableStateListOf<Float>().apply {
        repeat(Constants.NUM_HARMONICS){ this.add(0f) }
    }
}

@Composable
fun HarmonicSignalEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    Column(modifier) {
        RowOfVolumeSliders(
            modifier = Modifier.fillMaxHeight(0.50f),
            numSliders = Constants.NUM_HARMONICS,
            value = { sliderIndex -> viewModel.harmonicSliders[sliderIndex] },
            onValueChange = { sliderIndex, sliderValue ->
                val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue
                viewModel.harmonicSliders[sliderIndex] = newSliderValue
                viewModel.signal.value.harmonicSeries[sliderIndex+1] = newSliderValue.pow(3)
            }
        )
        Row(Modifier.border(1.dp, Color.White),) {
            XYPlot(
                modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .background(Color.Black)
                        .border(1.dp, Color.White),
                color = Color(0.4f, 0.0f, 1f, 1f),
                strokeWidth = 5f,
                data = viewModel.plotBuffer.value,
            )
            VerticalSlider(
                modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f),
                value = viewModel.bendAmount.value,
                valueRange = 0.5f..1.5f,
                onValueChange = {
                    viewModel.bendAmount.value = it
                    viewModel.signal.value.bend(it)
                },
                onValueChangeFinished = {
                    viewModel.signal.value.bend(1f)
                    viewModel.bendAmount.value = 1f
                }
            )
            Column {
                VerticalSlider(
                    modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .fillMaxWidth(),
                    value = viewModel.volume.value,
                    onValueChange = {
                        viewModel.volume.value = it
                        viewModel.signal.value.amp = it.pow(3)
                    }
                )
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        viewModel.signal.value.reset()
                        viewModel.signal.value.harmonicSeries.reset()
                        for(i in viewModel.harmonicSliders.indices){
                            viewModel.harmonicSliders[i] = 0f
                        }
                        for(i in viewModel.plotBuffer.value.indices){
                            viewModel.plotBuffer.value[i] = 0f
                        }
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
                VerticalValueSlider(
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
fun VerticalValueSliderScreen(
    modifier: Modifier = Modifier,
    initialValue: Float = 0f,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
){
    var amplitude by remember { mutableStateOf(initialValue) }

    VerticalValueSlider(
        modifier = modifier,
        value = amplitude,
        valueRange = valueRange,
        onValueChange = {
            amplitude = it
            onValueChange(it)
        },
        onValueChangeFinished = onValueChangeFinished
    )
}

@Composable
fun VerticalValueSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f

){
    Column(
        modifier = modifier.border(width = 1.dp, color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        VerticalSlider(
            modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished
        )
        Text(
            text = (value * 100).toInt().toString(),
            color = Color.White
        )
    }
}

@Composable
fun VerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
){
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        Slider(
            modifier = Modifier
                    .requiredWidth(this.maxHeight)
                    .requiredHeight(this.maxWidth)
                    .rotate(-90f),
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}