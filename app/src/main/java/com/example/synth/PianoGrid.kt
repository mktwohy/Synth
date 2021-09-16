package com.example.synth

import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.synth.Note.Companion.minus
import com.example.synth.Note.Companion.plus
import com.example.synth.Note.Companion.toList

class PianoGrid(
    val width: MutableState<Dp>,
    val height: MutableState<Dp>,
    var noteRange: ClosedRange<Note>
){
    val topRow = mutableListOf<Pair<Note, Dp>>()
    val bottomRow = mutableListOf<Pair<Note, Dp>>()

    fun recalculateWidths(){
        topRow.clear()
        bottomRow.clear()
        val whiteNotes = noteRange.toList().filter { it.name[1] == '_' }
        whiteNotes.forEach { whiteNote ->
            for((note, ratio) in topRowNoteRatios(whiteNote)){
                topRow.add(note to width.value * ratio/whiteNotes.size)
            }
            bottomRow.add(whiteNote to width.value / whiteNotes.size)
        }
    }

    fun findKeyAt(x: Dp, y: Dp): Note? {
        var cumSum: Dp
        fun searchRow(row: List<Pair<Note, Dp>>): Note?{
            cumSum = 0.dp
            for ((note, width) in row){
                if (x in cumSum..cumSum+width) return note
                else cumSum += width
            }
            return null
        }
        return when{
            y < 0.dp || y > height.value -> null
            y < height.value / 2 -> searchRow(topRow)
            else -> searchRow(bottomRow)
        }

    }

    private fun topRowNoteRatios(whiteNote: Note) =
        when(whiteNote.toString()[0]) {
            'C' -> listOf(whiteNote to 3/4f, (whiteNote + 1) to 1/4f)
            'D' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 1/2f, (whiteNote + 1) to 1/4f)
            'E' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 3/4f)
            'F' -> listOf(whiteNote to 3/4f, (whiteNote + 1) to 1/4f)
            'G' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 2/4f, (whiteNote + 1) to 1/4f)
            'A' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 2/4f, (whiteNote + 1) to 1/4f)
            'B' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 3/4f)
            else -> listOf()
        }
}
