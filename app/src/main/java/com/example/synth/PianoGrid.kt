package com.example.synth

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.minus
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.Note.Companion.toList

/** Defines the hitboxes for the piano **/
class PianoGrid(
    val viewModel: PianoViewModel
){
    val topRow = mutableStateListOf<Pair<Note, Dp>>()
    val bottomRow = mutableStateListOf<Pair<Note, Dp>>()

    fun recalculateWidths(){
        topRow.clear()
        bottomRow.clear()
        val whiteNotes = viewModel.noteRange.toList().filter { it.natural }
        whiteNotes.forEach { whiteNote ->
            for((note, ratio) in topRowNoteRatios(whiteNote)){
                topRow.add(note to viewModel.width * ratio/whiteNotes.size)
            }
            bottomRow.add(whiteNote to viewModel.width / whiteNotes.size)
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
            y < 0.dp || y > viewModel.height -> null
            y < viewModel.height / 2 -> searchRow(topRow)
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
