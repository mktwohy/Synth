package com.example.signallib

class SignalEngine {
    var waveShape       = WaveShape.SINE
    val harmonicSeries  = HarmonicSeries()

    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()

    init {
        // initialize noteToSignal
        // Note: This ensures that all keys for noteToSignal are not null.
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

    fun renderPcmToBuffer(buffer: FloatArray, notes: List<Note>, pitchBend: Float, amp: Float){
        // assign pitch bend to appropriate signals
        for(note in notes) {
            noteToSignal[note]!!.bendAmount = pitchBend
            noteToSignal[note]!!.amp = amp
        }

        for(i in buffer.indices){
            var sum = 0f
            for(note in notes){
                sum += noteToSignal[note]!!.evaluateNext()
            }
            buffer[i] = sum
        }
    }
}