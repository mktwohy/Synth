package com.example.synth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    data: FloatArray,
    normalizeValues: Boolean = false,
    color: Color = Color.Green,
    strokeWidth: Float = 3f
) {
    val xPlot = remember { FloatArray(data.size){ i -> i.toFloat() } }
    val yPlot = remember { FloatArray(data.size) }

    Canvas(modifier = modifier.fillMaxSize()) {
        //convert buffer to x and y values for plotting
        data.forEachIndexed { i, value ->
            yPlot[i] = (value * size.height) + (size.width/2)
        }

        //normalize each axis to fit inside bounds
        xPlot.normalize(0f, size.width)
        if(normalizeValues) yPlot.normalize(0f, size.height)

        //draw plot
        for(i in 0 until xPlot.size - 1){
            drawLine(
                start = Offset(x = xPlot[i], y = yPlot[i]),
                end = Offset(x = xPlot[i+1], y = yPlot[i+1]),
                color = color,
                strokeWidth = strokeWidth
            )
        }
    }
}
