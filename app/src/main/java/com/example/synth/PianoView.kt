package com.example.synth

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

data class Key(
    val name: Note,
    var signal: Signal,
    var isPressed: Boolean = false,
    val rects: MutableList<RectF> = mutableListOf()
    )

data class PianoGrid(
    val topRow      : List<RectF> = listOf(),
    val bottomRow   : List<RectF> = listOf()
)

class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    private val screen = (Resources.getSystem().displayMetrics)
    private val whiteWidth = screen.widthPixels / 7.toFloat()
    private val whiteHeight = screen.heightPixels.toFloat()
    private val blackWidth = whiteWidth / 2
    private val blackHeight = whiteHeight * 0.55f

    private val white = Paint().apply { setARGB(255, 255, 255, 255) }
    private val purple = Paint().apply { setARGB(255, 255, 0, 255) }
    private val black = Paint().apply {
        setARGB(255, 0, 0, 0)
        strokeWidth = whiteWidth / 80
    }

    private val keys: List<Key>
    private val pianoGrid: PianoGrid

    init{
        keys = createKeys(4 * 12..5 * 12)
        pianoGrid = createGrid()
        assignRectsToKeys()
    }

    private fun createKeys(noteRange: IntRange): List<Key>{
        Log.d("m_funCall","InitKeys!")
        val kList = mutableListOf<Key>().apply {
            val notes = Note.values().toList().subList(noteRange.first, noteRange.last)
            for (n in notes) {
                this.add(Key(n, SinSignal(n.freq)))
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

    override fun onDraw(canvas: Canvas?) {
        Log.d("m_funCall","onDraw!")
        super.onDraw(canvas)
        if (canvas == null) return

        for (k in keys) {
            for (rect in k.rects) {
                if (k.name.toString()[1] == '_')
                    canvas.apply {
                        drawRect(rect, white)
                        drawLine(rect.left, rect.top, rect.left, rect.bottom, black)
                    }
                else canvas.drawRect(rect, black)
            }
        }
    }
}