package com.example.synth

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
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
    private val black = Paint().apply {
        setARGB(255, 0, 0, 0)
        strokeWidth = whiteWidth / 80
    }
    private val purple = Paint().apply { setARGB(255, 255, 0, 255) }


    private val keys = initKeys(4 * 12 until 5 * 12)
    private fun initKeys(noteRange: IntRange) =
        mutableListOf<Key>().apply {
            val notes = Note.values().toList().subList(noteRange.first, noteRange.last)
            for (n in notes) {
                this.add(
                    Key(n, SinSignal(n.freq))
                )
                toList()
            }
        }

    private val pianoGrid = initGrid()
    private fun initGrid(): PianoGrid {
        fun initRow(top: Float, bottom: Float, widths: List<Float>): List<RectF> {
            val rects = mutableListOf<RectF>()
            var left = 0f
            for (width in widths) {
                rects.add(
                    RectF(left, top, left + width, bottom)
                )
            }
            return rects.toList()
        }

        val topWidths = mutableListOf<Float>().apply {
            add(whiteWidth * 3 / 4)
            repeat(3) { add(whiteWidth * 3 / 4) }
            repeat(2) { add(blackWidth) }
            repeat(2) { add(whiteWidth * 3 / 4) }
            repeat(5) { add(blackWidth) }
            add(whiteWidth * 3 / 4)
            toList()
        }

        val bottomWidths = mutableListOf<Float>().apply {
            repeat(7) { add(whiteWidth) }
            toList()
        }

        return PianoGrid(
            initRow(0f, whiteHeight, topWidths),
            initRow(whiteHeight, screen.heightPixels.toFloat(), bottomWidths)
        )
    }

    private fun assignRectsToKeys(pg: PianoGrid) {
        fun assignWhite(key: Key, topRowIndex: Int, bottomRowIndex: Int) {
            key.rects.apply {
                add(pg.bottomRow[bottomRowIndex])
                add(pg.topRow[topRowIndex])

            }
        }

        fun assignBlack(key: Key, topRowIndex: Int) {
            key.rects.add(pg.topRow[topRowIndex])
        }

        for (k in keys) {
            when (k.name.toString().substring(0, 1)) {
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
        super.onDraw(canvas)
        if (canvas == null) return

        assignRectsToKeys(pianoGrid)
        keys.forEach {
            for (rect in it.rects) {
                if (it.name.toString()[1] == '_') {
                    canvas.drawRect(rect, white)
                } else {
                    canvas.drawRect(rect, black)
                }
            }
        }
    }
}