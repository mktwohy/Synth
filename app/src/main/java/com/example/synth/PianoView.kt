
package com.example.synth

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnNextLayout
import com.example.synth.Note.Companion.transpose

/**
 * Stores information about each Key in the PianoView
 * @param note   The fundamental note name associated with the Key
 * @param color  The Key's Paint, which is either black or white
 * @param signal The Signal that will play when the note is pressed
 */
data class Key(
    var note: Note,
    val color: Color,
    var signal: Signal,
)

/**
 * A Grid of RectF objects that are used to draw each Key on screen. Additionally, each RectF
 * acts as a hitbox for its assigned key.
 *
 * The grid splits the keyboard in half, forming two rows. This means each key can simplified
 * as either one or two rectangles;
 * Every white key is made up of one Rect from the bottom row and one from the top row,
 * whereas every black key is made up of one Rect from the top row.
 *
 * @param width width of the PianoGrid
 * @param height height of the PianoGrid
 * @param octave octave of the keys in PianoGrid.
 *
 * @property keys a list of each Key object in the PianoGrid
 * @property topRow A list of 11 rectangles that visually define the top row
 * (made up of black keys and partial white keys)
 * @property bottomRow A list of 7 rects that visually define the bottom row
 * (made up of only white keys)
 * @property rectToKey maps each RectF object to its associated Key. This is
 * used to determine which key is pressed
 */
class PianoGrid(
    val width: Int,
    val height: Int,
    octave: Int
){
    val keys: List<Key>
    val topRow    : List<RectF>
    val bottomRow : List<RectF>
    val rectToKey : MutableMap<RectF, Key> = mutableMapOf()

    init{
        //Init keys
        keys =  Note.toList(octave).map { note ->
                Key(
                    note,
                    if (note.name[1] == '_') Color.WHITE else Color.BLACK,
                    SinSignal(note.freq)
                )
            }

        //Init rows
        val whiteWidth = width / 7.toFloat()
        val whiteHeight = height.toFloat()
        val blackWidth = whiteWidth / 2
        val blackHeight = whiteHeight / 2

        val topWidths = mutableListOf<Float>().apply {
            val topWhiteWidth = whiteWidth * 3 / 4
            add(topWhiteWidth)
            repeat(3) { add(blackWidth) }
            repeat(2) { add(topWhiteWidth) }
            repeat(5) { add(blackWidth) }
            add(topWhiteWidth)
        }

        val bottomWidths = mutableListOf<Float>().apply {
            repeat(7) { add(whiteWidth) }
        }

        fun createRow(top: Float, bottom: Float, widths: List<Float>) =
            mutableListOf<RectF>().apply {
                var left = 0f
                for (w in widths) {
                    add(RectF(left, top, left + w, bottom))
                    left += w
                }
            }

        topRow    = createRow(0f, blackHeight, topWidths)
        bottomRow = createRow(blackHeight, whiteHeight, bottomWidths)


        //Init rectToKey
        fun assignWhite(key: Key, topRowIndex: Int, bottomRowIndex: Int) {
            rectToKey[bottomRow[bottomRowIndex]] = key
            rectToKey[topRow[topRowIndex]]       = key
        }

        fun assignBlack(key: Key, topRowIndex: Int) {
            rectToKey[topRow[topRowIndex]] = key
        }

        for (k in keys) {
            when (k.note.toString().substring(0, 2)) {
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

    fun findKeyAt(x: Float, y: Float): Key? {
        fun searchRow(row: List<RectF>): Key?{
            for (rect in row)
                if (x in rect.left..rect.right)
                    return rectToKey[rect]
            return null
        }
        return if (y < height / 2)
            searchRow(topRow)
        else
            searchRow(bottomRow)
    }
}

/**
 * An interactive piano keyboard.
 *
 * It has a PianoGrid, which references a list of Key objects. When the PianoView detects touch
 * input, it uses the PianoGrid to detect which Keys have been pressed. The client can then view
 * this information by accessing the set of pressedKeys.
 * @property pressedKeys A set of Key objects which are currently pressed
 */
class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    var octave = 4 //Middle C by default. If you want to change this, use changeOctave()
    val audioEngine: AudioEngine
    private val pressedKeys = mutableSetOf<Key>()
    private lateinit var pianoGrid: PianoGrid

    init {
        rootView.doOnNextLayout {
            pianoGrid = PianoGrid(width, height, octave)
        }
        audioEngine = AudioEngine(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        for (r in pianoGrid.topRow + pianoGrid.bottomRow) {
            val k = pianoGrid.rectToKey[r]!!
            canvas.apply {
                drawRect(r, k.color.paint)
                if (k.color == Color.WHITE)
                    drawLine(r.left, r.top, r.left, r.bottom, Color.BLACK.paint)
                if (k in pressedKeys)
                    drawRect(r, Color.PURPLE.paint)
            }
        }
    }

    fun changeOctave(newOctave: Int){
        if (newOctave in 0..8) {
            val step = (newOctave - octave) * 12
            for (k in pianoGrid.keys){
                k.note = k.note.transpose(step)
                val newSignal = k.signal.transpose(step)
                k.signal = newSignal
            }
            octave = newOctave
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val previousPressedKeys = pressedKeys.toSet()
        pressedKeys.clear()

        for (i in 0 until event.pointerCount) {
            with(pianoGrid.findKeyAt(event.getX(i), event.getY(i))) {
                if (this != null) {
                    when (event.action) {

                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_POINTER_DOWN,
                        MotionEvent.ACTION_MOVE ->
                            pressedKeys.add(this)

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_POINTER_UP,
                        MotionEvent.ACTION_CANCEL ->
                            pressedKeys.remove(this)
                    }
                }
            }
        }

        if (pressedKeys != previousPressedKeys)
            audioEngine.updatePcm(pressedKeys)

        return true
    }


}