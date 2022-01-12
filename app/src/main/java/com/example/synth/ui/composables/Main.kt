package com.example.synth.ui.composables

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi


@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun Main(isPortrait: Boolean) {
    TestHarmonicDials()
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//
//        HarmonicSeriesEditor(
//            modifier = Modifier
//                .fillMaxHeight(if (isPortrait) 0.5f else 0.3f)
//                .border(1.dp, Color.White),
//            viewModel = AppModel.harmonicSeriesViewModel
//        )
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.4f)
//                .border(1.dp, Color.White)
//        ) {
//            SignalPlot(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth(0.9f)
//                    .background(Color.Black)
//                    .border(1.dp, Color.White),
//                viewModel = AppModel.SignalPlotViewModel,
//                color = Color(0.4f, 0.0f, 1f, 1f),
//                strokeWidth = 5f
//            )
//            WaveShapeSelector(
//                modifier = Modifier
//                    .fillMaxSize(),
//                viewModel = AppModel.waveFormChangeViewModel
//            )
//        }
//
//        Box(
//            Modifier
//                .fillMaxHeight(0.2f)
//                .fillMaxWidth(0.85f)) {
//            NoteRange(
//                viewModel = AppModel.pianoViewModel
//            )
//        }
//
//        Row(Modifier.border(1.dp, Color.White)) {
//            Piano(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth(if (isPortrait) 0.9f else 0.95f),
//                viewModel = AppModel.pianoViewModel,
//            )
//            PitchBend(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .border(1.dp, Color.White),
//                //viewModel = AppModel.pitchBendViewModel
//            )
//        }
//    }
}