package com.example.signallib

import com.example.signallib.enums.Note
import com.example.signallib.signalCollections.HarmonicSignal

/**
 * Abstracts away the creation and management of Signals. It maps a [HarmonicSignal] to each note,
 * and ensures that they all have the same [WaveShape] and [HarmonicSeries].
 *
 * How to use:
 * - set parameters [WaveShape] and [HarmonicSeries]
 * - call [renderToBuffer] with realtime parameters to get a single buffer of PCM data
 *
 */
class SignalManager(val signalSettings: SignalSettings) {
    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
    init {
        // initialize noteToSignal
        // Note: This ensures that all keys for noteToSignal are not null.
        for(note in Note.toList()){
            noteToSignal[note] = HarmonicSignal(
                fundamental     = note,
                amp             = 1f,
                autoNormalize   = false,
                signalSettings  = this.signalSettings
            )
        }
    }

    fun resetSignals(notes: Set<Note>){
        notes.forEach{ noteToSignal[it]?.reset() }
    }

    fun renderToBuffer(buffer: FloatArray, notes: Set<Note>, pitchBend: Float, amp: Float){
        // assign pitch bend to appropriate signals
        for(note in notes) {
            with(noteToSignal[note]){
                this?.bendAmount    = pitchBend
                this?.amp           = amp
            }
        }

        // sum signals
        for(i in buffer.indices){
            var sum = 0f
            for(note in notes){
                sum += noteToSignal[note]!!.evaluateNext()
            }
            buffer[i] = sum
        }
    }
}