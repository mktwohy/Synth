package com.example.signallib

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
        signalSettings.registerWaveShapeListener { waveShape ->
            for (note in Note.notes){
                noteToSignal[note]?.waveShape = waveShape
            }
        }

        // initialize noteToSignal
        // Note: This ensures that all keys for noteToSignal are not null.
        for(note in Note.toList()){
            noteToSignal[note] = HarmonicSignal(
                sampleRate      = signalSettings.sampleRate,
                fundamental     = note,
                waveShape       = signalSettings.waveShape,
                harmonicSeries  = signalSettings.harmonicSeries,
                amp             = 1f,
                autoNormalize   = false
            )
        }
    }

    fun renderToBuffer(buffer: FloatArray, notes: Set<Note>, pitchBend: Float, amp: Float){
        // assign pitch bend to appropriate signals
        for(note in Note.notes) {
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