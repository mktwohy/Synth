
package com.example.synth

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnNextLayout
import com.example.synth.Note.Companion.transpose

//https://stackoverflow.com/questions/49365350/java-create-a-custom-event-and-listener
interface PianoKeyEventListener{ fun onKeyUpdatedEvent(pressedPianoKeys: Set<PianoKey>) }

/** A wrapper class for Set<Key> that alerts its listeners when it's been updated */
class EventPianoKeySet{
    var pianoKeys = setOf<PianoKey>()
        set(newKeys) {
            field = newKeys
            listeners.forEach{ listener -> listener.onKeyUpdatedEvent(pianoKeys) }
        }
    private val listeners = mutableListOf<PianoKeyEventListener>()
    
    fun addPianoKeyListener(listener: PianoKeyEventListener){
        listeners.add(listener)
    }
}


/**
 * Stores information about each Key in the PianoView
 * @param note   The fundamental note name associated with the Key
 * @param color  The Key's Paint, which is either black or white
 * @param signal The Signal that will play when the note is pressed
 */
data class PianoKey(
    var note: Note,
    val color: Color,
    var signal: Signal
)

/**
 * A Grid of [RectF] objects that are used to draw each Key on screen. Additionally, each [RectF]
 * acts as a hitbox for its assigned [PianoKey].
 *
 *
 * The grid splits the keyboard in half, forming two rows. This structure is used so that every key
 * can be represented by 1-2 rectangles rather than a more complex shape;
 * Every white key is made up of one [RectF] from the bottom row and one from the top row,
 * whereas every black key is made up of one Rect from the top row.
 *
 * @param width width of the PianoGrid
 * @param height height of the PianoGrid
 * @param octave octave of the keys in PianoGrid.
 *
 * @property pianoKeys a list of each [PianoKey] in the PianoGrid
 * @property topRow A list of 11 rectangles that visually define the top row
 * (made up of black keys and partial white keys)
 * @property bottomRow A list of 7 rectangles that visually define the bottom row
 * (made up of only white keys)
 * @property rectToPianoKey maps each [RectF] to its associated [PianoKey]. This is
 * used to determine which key is pressed
 */
class PianoGrid(
    val width: Int,
    val height: Int,
    octave: Int
){
    val pianoKeys: List<PianoKey>
    val topRow    : List<RectF>
    val bottomRow : List<RectF>
    val rectToPianoKey : MutableMap<RectF, PianoKey> = mutableMapOf()

    init{
        //Init keys
        pianoKeys =  Note.toList(octave).map { note ->
                PianoKey(
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
        fun assignWhite(pianoKey: PianoKey, topRowIndex: Int, bottomRowIndex: Int) {
            rectToPianoKey[bottomRow[bottomRowIndex]] = pianoKey
            rectToPianoKey[topRow[topRowIndex]]       = pianoKey
        }

        fun assignBlack(pianoKey: PianoKey, topRowIndex: Int) {
            rectToPianoKey[topRow[topRowIndex]] = pianoKey
        }

        for (k in pianoKeys) {
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

    /** Uses PianoGrid as a hitbox to determine which [PianoKey] the user is touching */
    fun findKeyAt(x: Float, y: Float): PianoKey? {
        fun searchRow(row: List<RectF>): PianoKey?{
            for (rect in row)
                if (x in rect.left..rect.right)
                    return rectToPianoKey[rect]
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
 * It has a [PianoGrid], which references a list of [PianoKey]s. When the [PianoView] detects touch
 * input, it uses [PianoGrid.findKeyAt] to determine which [PianoKey]s have been pressed and update
 * [pressedKeys], which alerts its listeners.
 *
 * @property pressedKeys A set of Key objects that the user is currently pressing
 * @property octave Current of Keys on screen (middle C by default).
 * If you want to change this, use [changeOctave]
 */
class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    private lateinit var pianoGrid: PianoGrid
    val pressedKeys = EventPianoKeySet()
    var octave = 4
        set(newOctave){
            if (newOctave in 0..8) {
                val step = (newOctave - octave) * 12
                for (k in pianoGrid.pianoKeys){
                    k.note = k.note.transpose(step)
                    val newSignal = k.signal.transpose(step)
                    k.signal = newSignal
                }
                field = newOctave
            }
        }

    init {
        rootView.doOnNextLayout {
            pianoGrid = PianoGrid(width, height, octave)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        for (r in pianoGrid.topRow + pianoGrid.bottomRow) {
            val k = pianoGrid.rectToPianoKey[r]!!
            canvas.apply {
                drawRect(r, k.color.paint)
                if (k.color == Color.WHITE)
                    drawLine(r.left, r.top, r.left, r.bottom, Color.BLACK.paint)
                if (k in pressedKeys.pianoKeys)
                    drawRect(r, Color.PURPLE.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val newPressedKeys = mutableSetOf<PianoKey>()
        for (i in 0 until event.pointerCount) {
            val key = pianoGrid.findKeyAt(event.getX(i), event.getY(i))
            if (key != null) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_MOVE ->
                        newPressedKeys.add(key)

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_POINTER_UP,
                    MotionEvent.ACTION_CANCEL ->
                        newPressedKeys.remove(key)
                }
            }

        }

        if (pressedKeys.pianoKeys != newPressedKeys){
            pressedKeys.pianoKeys = newPressedKeys.toSet()
            this.postInvalidate()
        }

        return true
    }
}