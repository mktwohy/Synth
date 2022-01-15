package com.example.synth.viewModels

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.nextWhiteNote
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.Note.Companion.prevWhiteNote
import com.example.synth.logd
import com.example.synth.ui.composables.toFloat
import kotlin.math.abs
import kotlin.math.roundToInt

class PianoViewModel : ViewModel(){
    var pressedNotes by mutableStateOf(setOf<Note>())
    var width by mutableStateOf(0.dp)
    var height by mutableStateOf(0.dp)
    var noteRange by mutableStateOf(Note.C_4..Note.C_5)
    val pianoGrid = PianoGrid(this)

    @ExperimentalMaterialApi
    val swipeState = SwipeableState(0)
    var scaleState by mutableStateOf(12f)
    var rangeSliderState by mutableStateOf(
        noteRange.start.toFloat()..noteRange.endInclusive.toFloat()
    )

    fun changeNoteRangeScale(scale: Float){
        noteRange = noteRange.start..(noteRange.start + scale.roundToInt())
        pianoGrid.recalculateWidths()
    }

    fun changeNoteRangePosition(position: Int){
        logd(position)
    }

    fun shiftNoteRange(startOffset: Int, endOffset: Int){
        var start = noteRange.start
        var end = noteRange.endInclusive

        if(startOffset < 0)
            repeat(abs(startOffset)){ start = start.prevWhiteNote() }
        else if(startOffset > 0)
            repeat(abs(startOffset)){ start = start.nextWhiteNote() }

        if(endOffset < 0)
            repeat(abs(endOffset)){ end = end.prevWhiteNote() }
        else if(endOffset > 0)
            repeat(abs(endOffset)){ end = end.nextWhiteNote() }

        noteRange = start..end
        logd("Shift: $noteRange")
        pianoGrid.recalculateWidths()
    }

}