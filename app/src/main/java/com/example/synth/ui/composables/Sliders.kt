package com.example.synth.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    onVolumeChange: (Float) -> Unit = {}
){
    var sliderState by remember { mutableStateOf( 0.0f) }
    LabeledVerticalSlider(
        modifier = modifier,
        value = sliderState,
        onValueChange = {
            sliderState = it
            onVolumeChange.invoke(it)
        },
        showValue = true
    )
}

@Composable
fun RowOfVerticalSliders(
    modifier: Modifier = Modifier,
    numSliders: Int = 1,
    value: (Int) -> Float,
    onValueChange: (Int, Float) -> Unit,
    label: (Int) -> String,
    showValue: Boolean
){
    BoxWithConstraints(modifier = modifier){
        val sliderWidth = this.maxWidth/numSliders
        val sliderHeight = this.maxHeight

        Row(modifier = Modifier) {
            for(sliderIndex in 0 until numSliders){
                LabeledVerticalSlider(
                    modifier = Modifier.size(sliderWidth, sliderHeight),
                    value = value(sliderIndex),
                    onValueChange = { onValueChange(sliderIndex, it) },
                    label = label(sliderIndex),
                    showValue = showValue
                )
            }
        }
    }
}


@Composable
fun LabeledVerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String = "",
    showValue: Boolean = true
){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        val showLabel = label != ""
        VerticalSlider(
            modifier = Modifier
                .fillMaxHeight(
                    when {
                        showLabel && showValue -> 0.75f
                        showLabel xor showValue -> 0.85f
                        else -> 1f
                    }
                )
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished
        )
        if(showValue){
            Box(
                modifier = Modifier
                    .fillMaxHeight(if (showLabel) 0.5f else 1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = (value * 100).toInt().toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        if(showLabel){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = label,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
){
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        Slider(
            modifier = Modifier
                .requiredWidth(maxHeight)
                .requiredHeight(maxWidth)
                .rotate(-90f),
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )


    }
}