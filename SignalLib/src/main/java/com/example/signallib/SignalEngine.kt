package com.example.signallib

/** Abstracts away the creation and management of Signals */
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
                fundamental = note,
                waveShape = this.waveShape,
                harmonicSeries = this.harmonicSeries,
                amp = 1f,
                autoNormalize = false
            )
        }
    }

    fun renderPcmToBuffer(buffer: FloatArray, notes: Set<Note>, pitchBend: Float, amp: Float){
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

    fun registerOnWaveShapeChangedCallback(callback: (WaveShape) -> Unit){
        waveShapeCallbacks.add(callback)
    }
}