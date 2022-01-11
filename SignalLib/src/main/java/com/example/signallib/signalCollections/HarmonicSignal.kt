package com.example.signallib.signalCollections

import com.example.signallib.signals.PeriodicSignal
import com.example.signallib.SignalSettings
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.bend
import com.example.signallib.signals.Signal

class HarmonicSignal(
    fundamental: Note,
    amp: Float = 1f,
    autoNormalize: Boolean = true,
    signalSettings: SignalSettings
): SignalCollection(signalSettings) {
    override val size get() = signals.size
    override val period = signalSettings.sampleRate / fundamental.freq
    private val signals = List(signalSettings.harmonicSeries.numHarmonics){ i ->
        PeriodicSignal(
            frequency = fundamental.freq*(i+1),
            amp = 0f,
            signalSettings = this.signalSettings
        ).also {
            it.parents.add(this)
//            it.signalSettings = this.sampleRate
        }
    }
    var bendAmount: Float = 1f
        set(value){
            val bentFundFreq = fundamental.bend(value)
            for(i in signals.indices) {
                signals[i].frequency = bentFundFreq * (i+1)
            }
            field = value
        }

    var fundamental: Note = fundamental
        set(value){
            field = value
            for(i in signals.indices) {
                signals[i].frequency = fundamental.freq*(i+1)
            }
        }

    init {
        this.autoNormalize = autoNormalize
        this.amp = amp
        if(autoNormalize) normalize()

        signalSettings.registerHarmonicSeriesListener {
            for((overtone, amplitude) in it){
                signals[overtone-1].amp = amplitude
            }
            if(autoNormalize)
                normalize()
        }
    }

    override fun contains(element: Signal): Boolean = signals.contains(element)
    override fun containsAll(elements: Collection<Signal>): Boolean = signals.containsAll(elements)
    override fun isEmpty(): Boolean = signals.isEmpty()
    override fun iterator(): Iterator<Signal> = signals.iterator()
}