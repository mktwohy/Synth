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
fun VolumeSliderScreen(modifier: Modifier = Modifier, initialValue: Float){
    var amplitude by remember { mutableStateOf(initialValue) }

    Row(
        modifier = modifier
            .rotate(-90f)
            .wrapContentSize()
            .border(width = 2.dp, color = Color.White),
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth(0.1f)
                .rotate(90f),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = (amplitude*100).toInt().toString(),
                color = Color.White
            )
        }
        VolumeSlider(
            modifier = Modifier.border(width = 10.dp, color = Color.White),
            value = amplitude,
            onValueChange = { amplitude = it }
        )
    }
}



@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
){
    Slider(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
    )
}