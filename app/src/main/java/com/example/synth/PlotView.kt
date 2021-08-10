package com.example.synth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.android.material.transition.MaterialSharedAxis
import java.text.Normalizer.normalize


class PlotView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    var buffer = FloatArray(AudioEngine.BUFFER_SIZE)
    private val xValues = FloatArray(buffer.size){ i -> i.toFloat() }
    private val yValues = FloatArray(buffer.size)



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null) return


        buffer.forEachIndexed { i, value ->
            yValues[i] = value
        }
        yValues.normalize(0f, this.height.toFloat())
        xValues.normalize(0f, this.width.toFloat())

        for(i in 0 until xValues.size - 1){
            canvas.drawLine(
                xValues[i], yValues[i],
                xValues[i+1], yValues[i+1],
                Paints.PURPLE.paint
            )
        }
    }

}