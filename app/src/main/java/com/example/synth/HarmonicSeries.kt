package com.example.synth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


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
fun VolumeSliderOLD(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
){

    Column(
        modifier = modifier.border(width = 2.dp, color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Slider(
            modifier = Modifier
                .rotate(-90f)
                .fillMaxHeight(0.9f)
                .border(width = 10.dp, color = Color.White),
            value = value,
            onValueChange = onValueChange,
        )
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .fillMaxHeight(0.1f),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = (value*100).toInt().toString(),
                color = Color.White
            )
        }
    }
}

@Composable
fun VolumeSlider2(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
){

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .border(width = 2.dp, color = Color.White)
        ) {
            Slider(
                modifier = Modifier
                    .rotate(-90f)
                    .fillMaxSize()
                    .border(width = 3.dp, color = Color.Red)
                ,
                value = value,
                onValueChange = onValueChange,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .border(width = 2.dp, color = Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (value*100).toInt().toString(),
                color = Color.White
            )
        }

    }
}

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
){

    Box(modifier = modifier.border(width = 2.dp, color = Color.Red)){
        Row(
            modifier = Modifier
                .rotate(90f)
                .fillMaxSize()
                .border(width = 2.dp, color = Color.Green),
            verticalAlignment = Alignment.CenterVertically
        ){

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.White)
            ) {
                Slider(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(width = 3.dp, color = Color.Red)
                    ,
                    value = value,
                    onValueChange = onValueChange,
                )
            }

            Box(
                modifier = Modifier
                    .rotate(-90f)
                    .fillMaxSize()
                    .border(width = 2.dp, color = Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (value*100).toInt().toString(),
                    color = Color.White
                )
            }

        }
    }

}
