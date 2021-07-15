package com.example.synth

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import kotlin.system.measureTimeMillis

/**
 * An interactive piano keyboard
 *
 * A PianoView is made up of Keys which are visually represented by the PianoGrid
 * @property pressedKeys A set of Keys which are currently pressed
 */
class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    companion object{
        /**
         * Stores information about each Key in the PianoView
         * @param name   The fundamental note name associated with the Key
         * @param color  The Key's Paint, which is either black or white
         * @param signal The Signal that will play when the note is pressed
         */
        data class Key(
            val name: Note,
            val color: Paint,
            var signal: Signal,
        )

        /**
         * A Grid of RectF objects that are used to draw each Key on screen and identify which Key the user
         * touches. The grid is made up of two rows that horizontally cut the keyboard in half
         *
         * Every white key is made up of one Rect from the bottom row and one from the top row,
         * whereas every black key is made up of one Rect from the top row.
         *
         * @param topRow A list of 11 rectangles that visually define the top row
         * (black keys and partial white keys)
         * @param bottomRow A list of 7 rects that visually define the bottom row (only white keys)
         */
        data class PianoGrid(
            val topRow      : List<RectF> = listOf(),
            val bottomRow   : List<RectF> = listOf()
        )

        private val white  = Paint().apply { setARGB(255, 255, 255, 255) }
        private val purple = Paint().apply { setARGB(100, 255, 0, 255) }
        private val black  = Paint().apply { setARGB(255, 0, 0, 0) ; strokeWidth = 2f }

        private fun createKeys(octave: Int): List<Key> {
            return Note.notesInOctave(octave).map { note ->
                Key(
                    note,
                    if (note.name[1] == '_') white else black,
                    SinSignal(note.freq)
                )
            }
        }

        private fun createGrid(viewWidth: Int, viewHeight: Int): PianoGrid {
            val whiteWidth  = viewWidth / 7 .toFloat()
            val whiteHeight = viewHeight    .toFloat()
            val blackWidth  = whiteWidth  / 2
            val blackHeight = whiteHeight / 2

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

            fun initRow(top: Float, bottom: Float, widths: List<Float>) =
                mutableListOf<RectF>().apply {
                    var left = 0f
                    for(w in widths) {
                        add( RectF(left, top, left + w, bottom) )
                        left += w
                    }
                    toList()
                }

            return PianoGrid(
                initRow(0f, blackHeight, topWidths),
                initRow(blackHeight, whiteHeight, bottomWidths)
            )
        }

        private fun assignRectsToKeys(
            pianoGrid: PianoGrid,
            keys: List<Key>,
            rectToKey: MutableMap<RectF, Key>
        ) {
            fun assignWhite(key: Key, topRowIndex: Int, bottomRowIndex: Int) {
                rectToKey[pianoGrid.bottomRow[bottomRowIndex]] = key
                rectToKey[pianoGrid.topRow[topRowIndex]] = key
            }

            fun assignBlack(key: Key, topRowIndex: Int) {
                rectToKey[pianoGrid.topRow[topRowIndex]] = key
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
    }

    val pressedKeys = mutableSetOf<Key>()
    var pcmOutput: CircularShortArray = Signal.NullSignal.pcmData
    private val keys = createKeys(5)
    private val rectToKey = mutableMapOf<RectF, Key>()
    private lateinit var pianoGrid: PianoGrid

    init{
        rootView.doOnNextLayout {
            pianoGrid = createGrid(width, height)
            assignRectsToKeys(pianoGrid, keys, rectToKey)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        for (r in pianoGrid.topRow + pianoGrid.bottomRow) {
            val k = rectToKey[r]!!
            canvas.apply {
                drawRect(r, k.color)
                if (k.color == white) drawLine(r.left, r.top, r.left, r.bottom, black)
                if(k in pressedKeys)  drawRect(r, purple)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        pressedKeys.clear()

        fun getKey(x: Float, y: Float): Key?{
            if(y < height/2) {
                //search top row
                for (rect in pianoGrid.topRow)
                    if (x in rect.left..rect.right) return rectToKey[rect]
            }
            else {
                //search bottom row
                for (rect in pianoGrid.bottomRow)
                    if (x in rect.left..rect.right) return rectToKey[rect]
            }
            return null
        }

        for (i in 0 until event.pointerCount) {
            val key = getKey(event.getX(i), event.getY(i))
            if(key != null) {
                when (event.action) {

                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_MOVE ->
                        pressedKeys.add(key)

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_POINTER_UP,
                    MotionEvent.ACTION_CANCEL ->
                        pressedKeys.remove(key)
                }
            }
            else Log.d("m_nullKey", "key is null")
        }

        pcmOutput = pressedKeys
            .map { it.signal }
            .sum()      //todo this line is the bottleneck
            .pcmData

        return true
    }


}