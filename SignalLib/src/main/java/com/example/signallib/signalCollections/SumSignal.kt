package com.example.signallib.signalCollections

import com.example.signallib.SignalSettings
import com.example.signallib.lcm
import com.example.signallib.signals.Signal


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