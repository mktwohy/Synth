package com.example.signallib

import android.util.Log

/**
 * Abstracts away the creation and management of Signals.
 *
 * How to use:
 * - set parameters [WaveShape] and [HarmonicSeries]
 * - call [renderToBuffer] with realtime parameters to get a single buffer of PCM data
 *
 */
class SignalEngine(
    waveShape: WaveShape = WaveShape.SINE,
    val harmonicSeries: HarmonicSeries = HarmonicSeries()
) {
    var waveShape: WaveShape = waveShape
        set(value){
            Note.toList().forEach { noteToSignal[it]?.waveShape = value }
            waveShapeCallbacks.forEach { it.invoke(value) }
            field = value
        }

    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
    private val waveShapeCallbacks = mutableSetOf< (WaveShape) -> Unit >()


    init {
        // initialize noteToSignal
        // Note: This ensures that all keys for noteToSignal are not null.
        for(note in Note.toList()){
            noteToSignal[note] = HarmonicSignal(
                fundamental     = note,
                waveShape       = this.waveShape,
                harmonicSeries  = this.harmonicSeries,
                amp             = 1f,
                autoNormalize   = false
            )
        }
    }

    fun renderToBuffer(buffer: FloatArray, notes: Set<Note>, pitchBend: Float, amp: Float){
        // assign pitch bend to appropriate signals
        for(note in notes) {
            with(noteToSignal[note]){
                if(this != null){
                    this.bendAmount = pitchBend
                    this.amp = amp
                }
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

    fun registerOnWaveShapeChangedCallback(callback: (WaveShape) -> Unit){
        waveShapeCallbacks.add(callback)
    }
}