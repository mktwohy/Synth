package com.example.signallib.enums.enumGenerators

import com.example.signallib.enums.PitchClass
import java.io.File

// spreadsheet from https://pages.mtu.edu/~suits/NoteFreqCalcs.html

private data class NoteData(
    val freq: String,
    val pClass: String,
    val natural: String,
    val octave: String
)

private fun File.splitByLine() =
    this.readText()
        .split('\n')
        .map { it.removeSuffix("\n") }

private fun generateText(noteToNoteData: List<Pair<String, NoteData>>): String{
    val sb = StringBuilder()
    noteToNoteData.forEach{ (note, noteData) ->
        sb.append(
            note,
            " (",
            noteData.freq,
            ", ",
            noteData.pClass,
            ", ",
            noteData.natural,
            ", ",
            noteData.octave,
            "),\n"
        )
    }
    return sb.toString()
}

private fun String.formatNote() =
    if ('#' in this)
        this.replace("#", "s").substring(0..2)
    else
        "${this[0]}_${this[1]}".substring(0..2)



private fun main() {
    val path = System.getProperty("user.dir")!! +
            "/SignalLib/src/main/java/com/example/signallib/enums/enumGenerators"

    val notes = File("$path/NoteEnum_Notes.txt")
        .splitByLine()
        .map { it.formatNote() }
    val freqs = File("$path/NoteEnum_Freqs.txt")
        .splitByLine()
        .map { it.toDouble() }
        .map { it.toString() + 'f' }

    val noteToNoteData = notes.mapIndexed { i, note ->
        val natural = "s" !in note
        note to NoteData(
            freqs[i],
            if (natural) note[0].toString() else note.substring(0..1),
            natural.toString(),
            note[2].toString()
        )
    }

    with(File("$path/NoteEnum_Output.txt")){
        writeText(generateText(noteToNoteData))
        createNewFile()
    }
}