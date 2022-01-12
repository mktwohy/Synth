package com.example.synth.ui.composables

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.synth.viewModels.WaveShapeSelectorViewModel

@Composable
fun WaveShapeSelector(
    modifier: Modifier,
    viewModel: WaveShapeSelectorViewModel
){
    Button(
        modifier = modifier,
        onClick = { viewModel.nextWaveShape() }
    ) {
        Text(text = viewModel.waveShapeName)
    }
}