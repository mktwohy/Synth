package com.example.synth.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signallib.enums.HarmonicFilter
import com.example.synth.viewModels.HarmonicEditorViewModel

@ExperimentalComposeUiApi
@Composable
fun HarmonicEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicEditorViewModel
){
    Column(modifier) {
        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        Row(Modifier.fillMaxHeight(0.2f)){
            HarmonicDials(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f),
                viewModel = viewModel
            )
            FilterSelect(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
        }

        Row(Modifier.fillMaxSize()){
            HarmonicSliders(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(),
                viewModel = viewModel
            )
            HarmonicButtons(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White),
                viewModel = viewModel
            )
        }
    }

}


@Composable
private fun FilterSelect(modifier: Modifier, viewModel: HarmonicEditorViewModel){
    var oddState by remember { mutableStateOf(true) }
    var evenState by remember { mutableStateOf(true) }

    fun applyToViewModel(){
        val filters = mutableSetOf<HarmonicFilter>()

        viewModel.reset()

        if (evenState) {
            filters += HarmonicFilter.EVEN
            filters += HarmonicFilter.FUNDAMENTAL
        }
        if (oddState)
            filters += HarmonicFilter.ODD
        viewModel.filterState = filters

        viewModel.generate()
    }

    Column(modifier){
        Checkbox(
            checked = oddState,
            onCheckedChange = {
                oddState = it
                applyToViewModel()
            }
        )
        Checkbox(
            checked = evenState,
            onCheckedChange = {
                evenState = it
                applyToViewModel()
            }
        )
    }
}

@Composable
private fun HarmonicButtons(modifier: Modifier, viewModel: HarmonicEditorViewModel) {
    Column(modifier) {
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
            onClick = { viewModel.generateRandom() }
        ) {
            Text(
                text = "RNDM",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun HarmonicSliders(modifier: Modifier, viewModel: HarmonicEditorViewModel){
    RowOfVerticalSliders(
        modifier = modifier,
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
}

@ExperimentalComposeUiApi
@Composable
private fun HarmonicDials(
    modifier: Modifier,
    viewModel: HarmonicEditorViewModel
){
    BoxWithConstraints(modifier) {
        val dialHeight = this.maxHeight
        val dialWidth = this.maxWidth / 4

        Row{
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                value = viewModel.decayState,
                onValueChange = {
                    viewModel.decayState = it
                    viewModel.generate()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                value = viewModel.floorState,
                onValueChange = {
                    viewModel.floorState = it
                    viewModel.generate()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                value = viewModel.ceilingState,
                onValueChange = {
                    viewModel.ceilingState = it
                    viewModel.generate()
                }
            )
        }
    }
}

