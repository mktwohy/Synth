package com.example.synth.ui.composables

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.example.synth.logd
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
    limitingAngle: Float = 10f,
    lineColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    innerColor: Color = Color.DarkGray,
    value: Float,
    onValueChange: (Float) -> Unit
){
    var touchX by remember { mutableStateOf(0f) }
    var touchY by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier) {
        val widthDp = this.maxWidth
        val heightDp = this.maxHeight
        val centerX = with(LocalDensity.current){ widthDp.toPx() } / 2
        val centerY = with(LocalDensity.current){ heightDp.toPx() } / 2

        Canvas(
            modifier = modifier
                .pointerInteropFilter { event ->
                    touchX = event.x
                    touchY = event.y
                    val angle = -atan2(centerX - touchX, centerY - touchY) * (180 / PI).toFloat()
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            if (angle !in -limitingAngle..limitingAngle) {
                                val posAngle = toPositiveAngle(angle)

                                val percent = (posAngle - limitingAngle) / (360f - 2 * limitingAngle)
                                //logd("angle: $angle \t posAngle: $posAngle \t percent: $percent")
                                onValueChange(percent)
                                true
                            } else false
                        }
                        else -> false
                    }
                }
        ) {
            val radius = this.size.minDimension / 2f
            val innerRadius = radius * 0.95f

            rotate(toPositiveAngle(value * 180f) * 2) {
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
            drawLine(
                color = Color.Black,
                strokeWidth = 3f,
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
        limitingAngle = 5f,
        value = dialState,
        onValueChange = { dialState = it }
    )
}
