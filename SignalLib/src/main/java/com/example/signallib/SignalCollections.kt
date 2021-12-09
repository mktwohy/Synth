package com.example.signallib

import PeriodicSignal
import Signal
import com.example.signallib.Note.Companion.bend

abstract class SignalCollection(
    signalSettings: SignalSettings
): Signal(signalSettings), Collection<Signal> {
    var autoNormalize: Boolean = true

    fun normalize() {
        if (this.isEmpty())
            return
        val ampSum = this.map { it.amp }.sum()
        if(ampSum > amp)
            this.forEach { it.amp = (it.amp / ampSum) * amp  }
    }

    override fun reset() { this.forEach { it.reset() } }

    override fun evaluateNext(): Float{
        var sum = 0f
        this.forEach {
            if(it.amp != 0f)
                sum += it.evaluateNext()
        }
        return sum
    }

    override fun toString(): String{
        val s = StringBuilder()
        s.append(
            when(this){
                is SumSignal        -> ">SumSignal"
                is HarmonicSignal   -> ">HarmonicSignal"
                else                -> ">SignalCollection"
            }
        )
        s.append("(total amp = $amp):")
        this.forEach{
            s.append("\n\t")
            s.append("$it".replace("\n\t", "\n\t\t"))
        }
        return s.toString()
    }
}


class HarmonicSignal(
    fundamental: Note,
    amp: Float = 1f,
    autoNormalize: Boolean = true,
    signalSettings: SignalSettings
): SignalCollection(signalSettings) {
    override val size get() = signals.size
    override val period = signalSettings.sampleRate.value / fundamental.freq
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

        signalSettings.harmonicSeries.registerListener {
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

/** Combines two or more Signals into one Signal. */
class SumSignal(
    private val signals: MutableSet<Signal>,
    amp: Float = 1f,
    autoNormalize: Boolean = true,
    signalSettings: SignalSettings
) : SignalCollection(signalSettings), MutableCollection<Signal> by signals {
    //private val signals = mutableSetOf<Signal>()
    override val period
        get() = signals.map{ it.period.toInt() }.lcm().toFloat()

    init {
        this.autoNormalize = autoNormalize
        this.amp = amp
        this.signals.addAll(signals)
        if(autoNormalize)
            normalize()
    }

    operator fun plusAssign(that: Signal){
        when(that){
            is SignalCollection -> this.signals.addAll(that)
            else                -> this.signals.add(that)
        }
    }
//
//    override val size: Int get() = signals.size
//    override fun contains(element: Signal): Boolean = signals.contains(element)
//    override fun containsAll(elements: Collection<Signal>): Boolean = signals.containsAll(elements)
//    override fun isEmpty(): Boolean = signals.isEmpty()
//    override fun retainAll(elements: Collection<Signal>): Boolean = this.signals.retainAll(elements)
    override fun clear() {
        signals.forEach{ it.parents.remove(this) }
        signals.clear()
    }
//    override fun iterator(): MutableIterator<Signal> = signals.iterator()
    override fun remove(element: Signal): Boolean {
        if(signals.remove(element)){
            element.parents.remove(this)
            return true
        }
        if(autoNormalize) normalize()
        return false
    }

    override fun removeAll(elements: Collection<Signal>): Boolean {
        var removed = false
        elements.forEach{
            if(signals.remove(it)){
                removed = true
                it.parents.remove(this)
            }

        }
        return removed
    }

    override fun add(element: Signal): Boolean {
        if(element !in signals){
            element.parents.add(this)
            this.signals.add(element)
            this.normalize()
            return true
        }
        return false
    }

    override fun addAll(elements: Collection<Signal>): Boolean {
        var added = false
        elements.forEach{
            if(it !in signals){
                added = true
                it.parents.add(this)
                this.signals.add(it)
            }
        }
        if(autoNormalize) this.normalize()
        return added
    }
}