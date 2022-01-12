package com.example.synth.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synth.viewModels.HarmonicEditor

@Composable
fun HarmonicEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicEditor
){
    Row(modifier){
        RowOfVerticalSliders(
            modifier = Modifier.fillMaxWidth(0.9f),
            numSliders = viewModel.signalSettings.harmonicSeries.numHarmonics,
            value = { sliderIndex -> viewModel.sliderState[sliderIndex] },
            onValueChange = { sliderIndex, sliderValue ->
                val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue //snaps slider to 0
                viewModel.harmonicSeriesUpdateQueue.offer(sliderIndex to newSliderValue)
                viewModel.sliderState[sliderIndex] = newSliderValue
            },
            label = { index -> if(index == 0) "f" else "${index+1}" },
            showValue = false
        )
        Column(
            Modifier
                .fillMaxSize()
                .border(1.dp, Color.White)) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                onClick = { viewModel.reset() }
            ) {
                Text(
                    text = "RST",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            Button(
                modifier = Modifier.fillMaxSize(),
                onClick = { viewModel.random() }
            ) {
                Text(
                    text = "RNDM",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}