package com.example.synth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun RowOfVolumeSliders(
    modifier: Modifier = Modifier,
    numSliders: Int = 1
){
    BoxWithConstraints(modifier = modifier){
        val sliderWidth = this.maxWidth/numSliders
        val sliderHeight = this.maxHeight

        Row(modifier = Modifier) {
            repeat(numSliders){
                VolumeSliderScreen(modifier = Modifier.size(sliderWidth, sliderHeight))
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
            modifier = modifier.border(width = 2.dp, color = Color.White),
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