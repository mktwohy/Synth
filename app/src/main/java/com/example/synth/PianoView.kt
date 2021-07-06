package com.example.synth

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

data class Key(
    var signal: Signal,
    var rect: RectF,
    var isPressed: Boolean
)

class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    private val screen = (Resources.getSystem().displayMetrics)
    private val whiteWidth  = screen.widthPixels / 7 .toFloat()
    private val whiteHeight = screen.heightPixels    .toFloat()
    private val blackWidth = whiteWidth / 2
    private val blackHeight = whiteHeight * 0.55f

    private val white  = Paint().apply { setARGB(255, 255, 255, 255) }
    private val black  = Paint().apply {
        setARGB(255, 0, 0, 0)
        strokeWidth = whiteWidth/80
    }
    private val purple = Paint().apply { setARGB(255, 255, 0, 255) }

    private val whiteKeys = run{
        val wKeys = mutableListOf<Key>()
        var pixelStart = 0f
        repeat(7){
            wKeys.add(
                Key(
                    SinSignal(Note.C_4.freq),
                    RectF(pixelStart,0f, pixelStart + whiteWidth, whiteHeight),
                    false)
            )
            pixelStart += whiteWidth
        }
        wKeys.toList()
    }

    private val blackKeys = run{
        val bKeys = mutableListOf<Key>()
        for ((counter, wk) in whiteKeys.withIndex()){
            if (counter != 0 && counter != 3 && counter != 7)
                bKeys.add(
                Key(
                    SinSignal(Note.Cs4.freq),
                    RectF(wk.rect.left - (blackWidth / 2),
                        0f,
                        wk.rect.left + (blackWidth / 2),
                        blackHeight),
                    false
                )
            )
        }
        bKeys.toList()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return


        whiteKeys.forEach{
            canvas.apply{
                with(it.rect){
                    drawRect(this, white)
                    drawLine(left, top, left, bottom, if(it.isPressed) purple else black)
                }
            }
        }

        blackKeys.forEach{ canvas.drawRect(it.rect, if(it.isPressed) purple else black) }

    }




}