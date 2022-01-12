package com.example.synth.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.synth.SignalPlotViewModel

@Composable
fun SignalPlot(
    modifier: Modifier,
    viewModel: SignalPlotViewModel,
    color: Color = Color.Green,
    strokeWidth: Float = 3f
){
    XYPlot(
        modifier = modifier,
        data = viewModel.plotData,
        color = color,
        strokeWidth = strokeWidth
    )
}