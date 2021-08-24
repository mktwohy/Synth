package com.example.synth

class Oscillator(
    val harmonicSeries: HarmonicSeries,
){
    private var signal = SumSignal()
    private val onNotesUpdated = mutableSetOf<(Set<Note>) -> Unit>()
    private val notes = mutableSetOf<Note>()
    var waveShape: WaveShape = WaveShape.SINE

    fun registerOnNotesUpdatedCallback(callback: (Set<Note>) -> Unit){
        onNotesUpdated.add(callback)
    }

    fun updateNotes(newNotes: Collection<Note>){
        if(notes != newNotes){
            notes.addAll(newNotes)
            signal.signals.clear()
//            signal.signals.addAll()
            onNotesUpdated.forEach { it.invoke(notes) }
        }
    }
}