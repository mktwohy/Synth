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
fun VolumeSliderScreen(modifier: Modifier = Modifier, initialValue: Float = 0f){
    var amplitude by remember { mutableStateOf(initialValue) }

    VolumeSlider(
        modifier = modifier.border(width = 2.dp, color = Color.Magenta),
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
    Row(
        modifier = modifier
            .rotate(-90f)
            .fillMaxHeight()
            .wrapContentSize()
            .border(width = 2.dp, color = Color.White),
        verticalAlignment = Alignment.CenterVertically,
    ){
        Text(
            modifier = Modifier
                .fillMaxWidth(0.1f)
                .wrapContentWidth()
                .rotate(90f),
            text = (value * 100).toInt().toString(),
            color = Color.White
        )
        Slider(
            value = value,
            onValueChange = onValueChange
        )
    }
}

