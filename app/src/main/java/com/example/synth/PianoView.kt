package com.example.synth

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

data class Key(
    val name: Note,
    val color: Paint,
    var signal: Signal,
    val rects: MutableList<RectF> = mutableListOf()
    )

data class PianoGrid(
    val topRow      : List<RectF> = listOf(),
    val bottomRow   : List<RectF> = listOf()
)

class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    val pressedKeys = mutableSetOf<Key>()
    private val screen = (Resources.getSystem().displayMetrics)
    private val whiteWidth = screen.widthPixels / 7.toFloat()
    private val whiteHeight = screen.heightPixels.toFloat()
    private val blackWidth = whiteWidth / 2
    private val blackHeight = whiteHeight * 0.55f

    private val white = Paint().apply { setARGB(255, 255, 255, 255) }
    private val purple = Paint().apply { setARGB(100, 255, 0, 255) }
    private val black = Paint().apply {
        setARGB(255, 0, 0, 0)
        strokeWidth = whiteWidth / 80
    }

    private val keys: List<Key>
    private val pianoGrid: PianoGrid

    init{
        keys = createKeys(4 * 12 until 5 * 12)
        pianoGrid = createGrid()
        assignRectsToKeys()
    }

    private fun createKeys(noteRange: IntRange): List<Key>{
        Log.d("m_funCall","InitKeys!")
        val kList = mutableListOf<Key>().apply {
            val notes = Note.values().toList().subList(noteRange.first, noteRange.last+1)
            for (n in notes) {
                val color = if (n.name[1] == '_')
                    white else black
                add(Key(n, color, SinSignal(n.freq)))
            }
        }
        return kList.toList()
    }

    private fun createGrid(): PianoGrid {
        Log.d("m_funCall","initGrid!")
        fun initRow(top: Float, bottom: Float, widths: List<Float>): List<RectF> {
            val rects = mutableListOf<RectF>()
            var left = 0f
            for (width in widths) {
                rects.add(
                    RectF(left, top, left + width, bottom)
                )
                left += width
            }
            return rects.toList()
        }

        val topWidths = mutableListOf<Float>().apply {
            val topWhiteWidth = whiteWidth * 3 / 4
            add(topWhiteWidth)
            repeat(3) { add(blackWidth) }
            repeat(2) { add(topWhiteWidth) }
            repeat(5) { add(blackWidth) }
            add(topWhiteWidth)
            toList()
        }

        val bottomWidths = mutableListOf<Float>().apply {
            repeat(7) { add(whiteWidth) }
            toList()
        }

        return PianoGrid(
            initRow(0f, blackHeight, topWidths),
            initRow(blackHeight, whiteHeight, bottomWidths)
        )
    }

    private fun assignRectsToKeys() {
        Log.d("m_funCall","assignRectsToKeys!")
        fun assignWhite(key: Key, topRowIndex: Int, bottomRowIndex: Int) {
            key.rects.apply {
                add(pianoGrid.bottomRow[bottomRowIndex])
                add(pianoGrid.topRow[topRowIndex])
            }
        }

        fun assignBlack(key: Key, topRowIndex: Int) {
            key.rects.add(pianoGrid.topRow[topRowIndex])
        }

        for (k in keys) {
            when (k.name.toString().substring(0, 2)) {
                "C_" -> assignWhite(k, 0, 0)
                "Cs" -> assignBlack(k, 1)
                "D_" -> assignWhite(k, 2, 1)
                "Ds" -> assignBlack(k, 3)
                "E_" -> assignWhite(k, 4, 2)
                "F_" -> assignWhite(k, 5, 3)
                "Fs" -> assignBlack(k, 6)
                "G_" -> assignWhite(k, 7, 4)
                "Gs" -> assignBlack(k, 8)
                "A_" -> assignWhite(k, 9, 5)
                "As" -> assignBlack(k, 10)
                "B_" -> assignWhite(k, 11, 6)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        fun addPressedKey(x: Float, y: Float){
            pressedKeys.clear()

            for(key in keys) {
                for (rect in key.rects)
                    if ((x in rect.left..rect.right) && (y in rect.top..rect.bottom)) {
                        pressedKeys.add(key)
                    }
            }
        }

        for (i in 0 until event.pointerCount) {
            val x = event.getX(i)
            val y = event.getY(i)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> addPressedKey(x, y)
              MotionEvent.ACTION_MOVE -> { pressedKeys.clear()
                addPressedKey(event.x, event.y) }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> pressedKeys.clear()
            }
        }

        return true

    }



    override fun onDraw(canvas: Canvas?) {
        Log.d("m_funCall","onDraw!")
        super.onDraw(canvas)
        if (canvas == null) return

        for (k in keys) {
            for (rect in k.rects) {
                canvas.apply {
                    drawRect(rect, k.color)
                    if (k.color == white)
                        drawLine(rect.left, rect.top, rect.left, rect.bottom, black)
                    if(k in pressedKeys)
                        drawRect(rect, purple)
                }
            }
        }
    }
}