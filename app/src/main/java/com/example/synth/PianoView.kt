//
package com.example.synth
//
//import android.content.Context
//import android.graphics.*
//import android.util.AttributeSet
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import androidx.compose.ui.graphics.Color
//import androidx.constraintlayout.widget.ConstraintSet
//import androidx.core.view.doOnNextLayout
//import com.example.synth.Note.Companion.color
//import kotlin.system.measureTimeMillis
//
//
//
////https://stackoverflow.com/questions/49365350/java-create-a-custom-event-and-listener
//interface PianoKeyEventListener{ fun onKeyUpdatedEvent(pressedPianoKeys: Set<Note>) }
//
///** A wrapper class for Set<[Note]> that alerts its listeners when it's been updated */
//class EventPianoKeySet{
//    var pianoKeys = setOf<Note>()
//        set(newKeys) {
//            field = newKeys
//            listeners.forEach{ listener -> listener.onKeyUpdatedEvent(pianoKeys) }
//        }
//    private val listeners = mutableListOf<PianoKeyEventListener>()
//
//    fun addPianoKeyListener(listener: PianoKeyEventListener){
//        listeners.add(listener)
//    }
//}
//
///**
// * A Grid of [RectF] objects that are used to draw each Key on screen. Additionally, each [RectF]
// * acts as a hitbox for its assigned [Note].
// *
// *
// * [This grid layout](https://github.com/mktwohy/Synth/blob/master/Images/PianoGridBlueprint.jpg)
// * is used so that every key can be represented by 1-2 rectangles (white: 2 rectangles, black: 1
// * rectangle) rather than a more complex shape.
// *
// *
// * @param width width of the PianoGrid
// * @param height height of the PianoGrid
// * @param octave octave of the keys in PianoGrid.
// *
// * @property pianoKeys a list of each [Note] in the PianoGrid
// * @property topRow A list of 11 rectangles that visually define the top row
// * (made up of black keys and partial white keys)
// * @property bottomRow A list of 7 rectangles that visually define the bottom row
// * (made up of only white keys)
// * @property rectToPianoKey maps each [RectF] to its associated [Note]. This is
// * used to determine which key is pressed
// */
//class PianoGrid(
//    val width: Int,
//    val height: Int,
//    octave: Int
//){
//    var pianoKeys: List<Note> = Note.toList(octave)
//        set(value){
//            field = value
//            mapRectsToKeys()
//        }
//    val topRow    : List<RectF>
//    val bottomRow : List<RectF>
//    val rectToPianoKey : MutableMap<RectF, Note> = mutableMapOf()
//
//    // This part is pretty lengthy. See link in [PianoGrid] documentation for a picture of what this
//    // init creates. It's not too important, so you can just fold it and not worry about it.
//    init{
//        //Init rows
//        val whiteWidth = width / 7.toFloat()
//        val whiteHeight = height.toFloat()
//        val blackWidth = whiteWidth / 2
//        val blackHeight = whiteHeight / 2
//
//        val topWidths = mutableListOf<Float>().apply {
//            val topWhiteWidth = whiteWidth * 3 / 4
//            add(topWhiteWidth)
//            repeat(3) { add(blackWidth) }
//            repeat(2) { add(topWhiteWidth) }
//            repeat(5) { add(blackWidth) }
//            add(topWhiteWidth)
//        }
//
//        val bottomWidths = mutableListOf<Float>().apply {
//            repeat(7) { add(whiteWidth) }
//        }
//
//        fun createRow(top: Float, bottom: Float, widths: List<Float>) =
//            mutableListOf<RectF>().apply {
//                var left = 0f
//                for (w in widths) {
//                    add(RectF(left, top, left + w, bottom))
//                    left += w
//                }
//            }
//
//        topRow    = createRow(0f, blackHeight, topWidths)
//        bottomRow = createRow(blackHeight, whiteHeight, bottomWidths)
//
//
//        //Init rectToKey
//        mapRectsToKeys()
//    }
//
//    private fun mapRectsToKeys(){
//        var topRowIndex = 0
//        var bottomRowIndex = 0
//
//        for (key in pianoKeys) {
//            if (key.color() == Color.White) {
//                rectToPianoKey[topRow[topRowIndex]]       = key
//                rectToPianoKey[bottomRow[bottomRowIndex]] = key
//                topRowIndex++
//                bottomRowIndex++
//            }else{
//                rectToPianoKey[topRow[topRowIndex]] = key
//                topRowIndex++
//            }
//        }
//    }
//
//    /** Uses PianoGrid as a hitbox to determine which [Note] the user is touching */
//    fun findKeyAt(x: Float, y: Float): Note? {
//        fun searchRow(row: List<RectF>): Note?{
//            for (rect in row)
//                if (x >= rect.left && x <= rect.right)
//                    return rectToPianoKey[rect]
//            return null
//        }
//        return if (y < height / 2)
//            searchRow(topRow)
//        else
//            searchRow(bottomRow)
//    }
//}
//
///**
// * An interactive piano keyboard.
// *
// * It has a [PianoGrid], which references a list of [Note]s. When the [PianoView] detects touch
// * input, it uses [PianoGrid.findKeyAt] to determine which [Note]s have been pressed and update
// * [pressedKeys], which alerts its listeners.
// *
// * @property noise Controls the amount of harmonic noise applied to currentAudio.
// * The noise is a result of manipulating [CircularIntArray.nextChunk]
// * @property pressedKeys A set of Key objects that the user is currently pressing
// * @property octave Current of Keys on screen (middle C by default).
// */
//class PianoView(context: Context, attrs: AttributeSet)
//    : View(context, attrs) {
//
//    val pressedKeys = EventPianoKeySet()
//    lateinit var pianoGrid: PianoGrid
//    var octave = 4
//        set(value){
//            if (value in 0..8) {
//                pianoGrid.pianoKeys = Note.toList(value)
//                field = value
//            }
//        }
//
//    init {
//        rootView.doOnNextLayout {
//            pianoGrid = PianoGrid(width, height, octave)
//        }
//
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        if (canvas == null) return
//
////        for (r in pianoGrid.topRow + pianoGrid.bottomRow) {
////            val k = pianoGrid.rectToPianoKey[r]!!
////            canvas.apply {
////                drawRect(r, k.color())
////                if (k.color() == Color.White)
////                    drawLine(r.left, r.top, r.left, r.bottom, Paints.BLACK.paint)
////                if (k in pressedKeys.pianoKeys)
////                    drawRect(r, Paints.PURPLE.paint)
////            }
////        }
//    }
//
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if (event == null) return false
//
//        val newPressedKeys = mutableSetOf<Note>()
//
//        for (i in 0 until event.pointerCount) {
//            val key = pianoGrid.findKeyAt(event.getX(i), event.getY(i))
//            if (key != null) {
//                if(i == event.actionIndex){
//                    when (event.actionMasked) {
//                        MotionEvent.ACTION_DOWN,
//                        MotionEvent.ACTION_MOVE ->
//                            newPressedKeys.add(key)
//
//                        MotionEvent.ACTION_UP,
//                        MotionEvent.ACTION_CANCEL ->
//                            newPressedKeys.remove(key)
//                    }
//                }else{
//                    newPressedKeys.add(key)
//                }
//            }
//        }
//
//        if (pressedKeys.pianoKeys != newPressedKeys){
//            pressedKeys.pianoKeys = newPressedKeys.toSet()
//            this.postInvalidate()
//        }
//
//        return true
//    }
//}