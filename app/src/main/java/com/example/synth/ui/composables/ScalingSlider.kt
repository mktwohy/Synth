package com.example.synth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.synth.logd
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun TestScalingSlider(){
    var scaleState by remember { mutableStateOf(12f) }
    var valueState by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        ScalingSlider(
            modifier = Modifier,
            resolution = 10
        )
    }

}

@Composable
fun ScalingSlider(
    modifier: Modifier,
    resolution: Int,
){
    var offsetXState by remember { mutableStateOf(0f) }
    var scaleState by remember { mutableStateOf(1f) }

    BoxWithConstraints(modifier) {
        val width = this.maxWidth
        val squareWidth = this.maxWidth / resolution

        Box(
            Modifier
                .fillMaxHeight(0.3f)
                .fillMaxWidth()
                .background(Color.White)
                .pointerInput(Unit){
                    detectTransformGestures { _, pan, zoom, _ ->
                        scaleState *= zoom
                        offsetXState = (offsetXState + pan.x)
                            .coerceIn(0f, (width - (squareWidth * scaleState)).toPx())
                    }
                }
        ) {
            Box(
                Modifier
                    .offset { IntOffset(offsetXState.roundToInt(), 0) }
                    .width(squareWidth * scaleState)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
            )
        }
    }


}

// based off of SwipeableSample: https://developer.android.com/jetpack/compose/gestures
@Deprecated("too confusing and it doesn't work")
@ExperimentalMaterialApi
@Composable
fun ScalingSlider2(
    modifier: Modifier,
    resolution: Int,
    value: Float,
    scale: Float,
    onValueChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val width       = this.maxWidth
        val squareWidth = this.maxWidth / resolution

        val widthPx = with(LocalDensity.current) { width.toPx() }
//        val squareWidthPx = with(LocalDensity.current) { squareWidth.toPx() }

        val position = value * widthPx

//        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
//            onScaleChange(scale * zoomChange)
//            onValueChange(value + (panChange.x.dp / width))
//        }

        Box(
            modifier = Modifier
                .width(width)
                .background(Color.LightGray)
//                .transformable(transformState)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        onValueChange(value + dragAmount.x)
                        logd("dragAmount: $dragAmount")
                    }
                    detectTransformGestures { centroid, pan, zoom, rotation -> }
                }
        ) {
            Box(
                Modifier
                    .offset { IntOffset(position.roundToInt(), 0) }
//                    .graphicsLayer {
//                        scaleX = scale
//                        translationX = position
//                    }
                    .width(squareWidth * scale)
                    .fillMaxHeight()
                    .background(Color.DarkGray)

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




