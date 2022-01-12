package com.example.synth.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.example.synth.AppModel
import com.example.synth.viewModels.HarmonicDialsViewModel
import com.example.synth.viewModels.HarmonicEditor

@ExperimentalComposeUiApi
@Composable
fun HarmonicDials(
    modifier: Modifier,
    viewModel: HarmonicDialsViewModel
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
                    viewModel.updateHarmonicSeries()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                value = viewModel.floorState,
                onValueChange = {
                    viewModel.floorState = it
                    viewModel.updateHarmonicSeries()
                }
            )
            Dial(
                modifier = Modifier.size(dialWidth, dialHeight),
                value = viewModel.ceilingState,
                onValueChange = {
                    viewModel.ceilingState = it
                    viewModel.updateHarmonicSeries()
                }
            )
        }
    }

}

@ExperimentalComposeUiApi
@Composable
fun TestHarmonicDials(){
    Column(Modifier.fillMaxSize()) {
        val signalSettings = AppModel.signalSettings
        HarmonicDials(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.50f),
            viewModel = HarmonicDialsViewModel(signalSettings)
        )
        HarmonicEditor(
            modifier = Modifier.fillMaxSize(),
            viewModel = HarmonicEditor(signalSettings)
        )
    }


}