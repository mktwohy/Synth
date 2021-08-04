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
import java.text.Normalizer.normalize


class PlotView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    var buffer = IntArray(AudioEngine.BUFFER_SIZE){ i -> i }
    val xValues = FloatArray(buffer.size){ i -> i.toFloat() }
    val yValues = FloatArray(buffer.size)



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null) return


        buffer.forEachIndexed { i, value ->
            yValues[i] = this.height - value.toFloat()
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