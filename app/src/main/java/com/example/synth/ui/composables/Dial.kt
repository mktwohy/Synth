package com.example.synth.ui.composables

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2


private fun toPositiveAngle(angle: Float) =
    if (angle in 0f..180f) angle
    else 360f + angle


@ExperimentalComposeUiApi
@Composable
/** Based off of [Phillip Lackner's music knob](https://www.youtube.com/watch?v=TOflUdgx4pw) */
fun Dial(
    modifier: Modifier,
    lineColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    innerColor: Color = Color.DarkGray,
    value: Float,
    onValueChange: (Float) -> Unit
){
    var touchX by remember { mutableStateOf(0f) }
    var touchY by remember { mutableStateOf(0f) }
    var centerX by remember { mutableStateOf(0f) }
    var centerY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = modifier
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow() //if reusing, might need to change this
                centerX = windowBounds.size.width / 2f
                centerY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event ->
                touchX = event.x
                touchY = event.y
                val angle = -atan2(centerX - touchX, centerY - touchY) * (180 / PI).toFloat()

                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        val posAngle = toPositiveAngle(angle)

                        val percent = (if (posAngle < 0f) posAngle + 180f else posAngle) / 360f

                        onValueChange(percent)
                        true
                    }
                    else -> false
                }
            }
    ){
        val radius = this.size.minDimension/2f
        val innerRadius = radius * 0.95f

        rotate(toPositiveAngle(value * 360f)){
            drawCircle(
                color = borderColor,
                radius = radius
            )
            drawCircle(
                color = innerColor,
                radius = innerRadius
            )
            drawLine(
                color = lineColor,
                strokeWidth = 4f,
                start = this.center,
                end = Offset(radius, center.y - innerRadius)
            )
        }
    }

}

@ExperimentalComposeUiApi
@Composable
fun TestDial(){
    var dialState by remember { mutableStateOf(0f) }
    Text(text = dialState.toString(), fontSize = 24.sp, color = Color.White)

    Dial(
        modifier = Modifier.fillMaxSize(),
        value = dialState,
        onValueChange = { dialState = it }
    )
}
