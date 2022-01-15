package com.example.synth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun TestScalingSlider(){
    val positionState = rememberSwipeableState(0)
    var scaleState by remember { mutableStateOf(12f) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
//        ScalingSlider(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.25f),
//            value = 1f,
//            scale = 1f,
//            onValueChange = { },
//            onScaleChange = { }
//        )
        ScalingSlider(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f),
            resolution = 88,
            position = positionState,
            scale = scaleState,
            onScaleChange = { scaleState = it }
        )
    }
}

// based off of SwipeableSample: https://developer.android.com/jetpack/compose/gestures
@ExperimentalMaterialApi
@Composable
fun ScalingSlider(
    modifier: Modifier,
    resolution: Int,
    position: SwipeableState<Int>,
    scale: Float,
    onScaleChange: (Float) -> Unit
) {
    BoxWithConstraints(modifier) {
        val transformState = rememberTransformableState { zoomChange, _, _ ->
            onScaleChange(scale * zoomChange)
        }

        val width       = this.maxWidth
        val squareWidth = this.maxWidth / resolution

        val sizePx = with(LocalDensity.current) { squareWidth.toPx() }
        val anchors = List(resolution){ index -> index }.map { sizePx * it to it}.toMap()

        Box(
            modifier = Modifier
                .width(width)
                .swipeable(
                    state = position,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
                .background(Color.LightGray)
                .transformable(transformState)
        ) {
            Box(
                Modifier
                    .offset { IntOffset(position.offset.value.roundToInt(), 0) }
                    .width(squareWidth * scale)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
                    .graphicsLayer {
                        scaleX = scale
                    }
            )
        }
    }
}


@Composable
fun ScalingSliderOLD(
    modifier: Modifier,
    value: Float,
    scale: Float,
    onValueChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.border(2.dp, Color.White),
        contentAlignment = Alignment.Center
    ) {
        ScalingSliderBox(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.2f),
            value = 1f,
            scale = 1f,
            onValueChange = { },
            onScaleChange = { }
        )
    }
}

@Composable
private fun ScalingSliderBox(
    modifier: Modifier,
    value: Float,
    scale: Float,
    onValueChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
){
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    offsetX += dragAmount.x
                }
            }
    )

}




