package com.example.signallib

import HarmonicSignal

class SignalEngine {
    var waveShape       = WaveShape.SINE
    var harmonicSeries  = HarmonicSeries()

    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()

    init {
        // initialize noteToSignal
        for(note in Note.toList()){
            noteToSignal[note] = HarmonicSignal(
                fundamental = note,
                waveShape = this.waveShape,
                harmonicSeries = this.harmonicSeries,
                amp = 1f,
                autoNormalize = false
            )
        }
    }

    fun renderPcmToBuffer(buffer: FloatArray, notes: List<Note>, pitchBend: Float){
        // assign pitch bend to appropriate signals
        notes.forEach { noteToSignal[it]?.bendAmount = pitchBend }

        for(i in buffer.indices){
            var sum = 0f
            for(note in notes){
                sum += noteToSignal[note]?.evaluateNext() ?: 0f
            }
            buffer[i] = sum
        }
    }
}