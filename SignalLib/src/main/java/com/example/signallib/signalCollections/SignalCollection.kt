package com.example.signallib.signalCollections

import com.example.signallib.SignalSettings
import com.example.signallib.signals.Signal

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
                is SumSignal -> ">SumSignal"
                is HarmonicSignal -> ">HarmonicSignal"
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