
package com.example.synth

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnNextLayout
import com.example.synth.Note.Companion.transpose

//https://stackoverflow.com/questions/49365350/java-create-a-custom-event-and-listener
interface KeyUpdateEventListener{ fun onKeyUpdatedEvent(pressedKeys: Set<Key>) }

/** A set of Keys that alerts its listeners when it's been updated */
class EventKeySet{
    private var keySet = setOf<Key>()
    private val eventListeners = mutableListOf<KeyUpdateEventListener>()
    
    fun addKeyUpdateListener(evtListener: KeyUpdateEventListener){
        eventListeners.add(evtListener)
    }

    fun updateKeySet(newSet: Set<Key>): Boolean{
        if(keySet != newSet){
            keySet = newSet
            eventListeners.forEach{ listener -> listener.onKeyUpdatedEvent(keySet) }
            return true
        }
        return false
    }

    fun getKeySet(): Set<Key> {
        return keySet
    }
}


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
 * A Grid of [RectF] objects that are used to draw each Key on screen. Additionally, each [RectF]
 * acts as a hitbox for its assigned [Key].
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
 * @property keys a list of each [Key] in the PianoGrid
 * @property topRow A list of 11 rectangles that visually define the top row
 * (made up of black keys and partial white keys)
 * @property bottomRow A list of 7 rectangles that visually define the bottom row
 * (made up of only white keys)
 * @property rectToKey maps each [RectF] to its associated [Key]. This is
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

    /** Uses PianoGrid as a hitbox to determine which [Key] the user is touching */
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
 * It has a [PianoGrid], which references a list of [Key]s. When the [PianoView] detects touch
 * input, it uses [PianoGrid.findKeyAt] to determine which [Key]s have been pressed and update
 * [pressedKeys], which alerts its listeners.
 *
 * @property pressedKeys A set of Key objects that the user is currently pressing
 * @property octave Current of Keys on screen (middle C by default).
 * If you want to change this, use [changeOctave]
 */
class PianoView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    private lateinit var pianoGrid: PianoGrid
    val pressedKeys = EventKeySet()
    var octave = 4
        set(newOctave){
            if (newOctave in 0..8) {
                val step = (newOctave - octave) * 12
                for (k in pianoGrid.keys){
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
            val k = pianoGrid.rectToKey[r]!!
            canvas.apply {
                drawRect(r, k.color.paint)
                if (k.color == Color.WHITE)
                    drawLine(r.left, r.top, r.left, r.bottom, Color.BLACK.paint)
                if (k in pressedKeys.getKeySet())
                    drawRect(r, Color.PURPLE.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val newPressedKeys = mutableSetOf<Key>()
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

        if (pressedKeys.updateKeySet(newPressedKeys.toSet()) ){
            this.postInvalidate()
        }

        return true
    }



}