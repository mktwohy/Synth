package com.example.synth.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synth.plus
import com.example.synth.ui.theme.Purple200
import com.example.synth.ui.theme.Purple500
import com.example.synth.ui.theme.Purple700
import com.example.synth.viewModels.HarmonicEditorViewModel

@ExperimentalComposeUiApi
@Composable
fun HarmonicEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicEditorViewModel
){
    Column(modifier) {
//        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        Row(Modifier.fillMaxWidth().fillMaxHeight(0.8f)){
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
        Row(Modifier.fillMaxSize()){
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
    }
}


@Composable
private fun FilterSelect(
    modifier: Modifier,
    viewModel: HarmonicEditorViewModel,
    buttonColor: Color = Purple500,
    textColor: Color = Color.White,
    overlayColor: Color = Color.Black,
){
    val onToggle: (Boolean) -> Unit = {
        viewModel.editHarmonicSeries { hs ->
            hs.onEach{ (harm, _) -> hs[harm] = 0f }
        }
        viewModel.updateSliders()
        viewModel.applyDialsAndFilters()
    }
    Column(modifier){
        ToggleButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .border(color = Color.White, width = 2.dp),
            toggled = viewModel.evenState,
            onToggle = {
                viewModel.evenState = it
                onToggle(it)
            },
            color = buttonColor
        ){
            Box(modifier = modifier, contentAlignment = Alignment.Center){
                Text(
                    text = "Evens",
                    color = if (viewModel.evenState) textColor else textColor + overlayColor
                )
            }
        }
        ToggleButton(
            modifier = Modifier
                .fillMaxSize()
                .border(color = Color.White, width = 1.dp),
            toggled = viewModel.oddState,
            onToggle = {
                viewModel.oddState = it
                onToggle(it)
            },
            color = buttonColor
        ){
            Box(modifier = modifier, contentAlignment = Alignment.Center){
                Text(
                    text = "Odds",
                    color = if (viewModel.oddState) textColor else textColor + overlayColor
                )
            }
        }
    }
}

@Composable
private fun HarmonicButtons(modifier: Modifier, viewModel: HarmonicEditorViewModel) {
    Column(modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .border(color = Color.White, width = 1.dp),
            onClick = { viewModel.resetDialsAndFilters() }
        ) {
            Text(
                text = "RST",
                color = Color.White,
                fontSize = 11.sp
            )
        }
        Button(
            modifier = Modifier
                .fillMaxSize()
                .border(color = Color.White, width = 1.dp),
            onClick = { viewModel.randomizeDialsAndFilters() }
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
            viewModel.editHarmonicSeries(sliderIndex, newSliderValue)
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
        val dialWidth = this.maxWidth / 3

        Row(
            modifier = Modifier.border(color = Color.White, width = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                limitingAngle = 2f,
                value = viewModel.decayState,
                onValueChange = {
                    viewModel.decayState = it
                    viewModel.applyDialsAndFilters()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                limitingAngle = 2f,
                value = viewModel.floorState,
                onValueChange = {
                    viewModel.floorState = it
                    viewModel.applyDialsAndFilters()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                limitingAngle = 2f,
                value = viewModel.ceilingState,
                onValueChange = {
                    viewModel.ceilingState = it
                    viewModel.applyDialsAndFilters()
                }
            )
        }
    }
}

