package com.example.synth.ui.composables

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.synth.AppModel

@Composable
fun PitchBend(
    modifier: Modifier = Modifier,
){
    var sliderState by remember { mutableStateOf( 0.0f) }
    VerticalSlider(
        modifier = modifier,
        value = sliderState,
        valueRange = -1f..1f,
        onValueChange = {
            sliderState = it
            AppModel.signalEngine.updatePitchBend(it)
        },
        onValueChangeFinished = {
            sliderState = 0f
            AppModel.signalEngine.updatePitchBend(0f) //snap back to 0f
        },
    )
}