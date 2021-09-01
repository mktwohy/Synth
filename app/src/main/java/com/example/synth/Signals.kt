package com.example.synth


import com.example.synth.Constants.SAMPLE_RATE
import com.example.synth.Note.Companion.bend

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal{
    val parents = mutableSetOf<SignalCollection>()
    abstract val period: Float
    var amp: Float = 1f
        set(value){
            when{
                value >= 0f -> field = value
                value.isNaN() -> field = 0f
            }
            if(this is SignalCollection){
                normalize()
            }
        }

    /** Resets the internal angle,
     * which guarantees that [evaluateNext] starts at the beginning */
    abstract fun reset()

    /** Uses the Signal's Clock to evaluate the next value in the Signal's sequence */
    abstract fun evaluateNext(): Float

    /** Evaluates the next n periods of the signal as a new array */
    fun evaluate(periods: Int): FloatArray{
        val ret = FloatArray(period.toInt() * periods)
        evaluateToBuffer(ret)
        return ret
    }

    /** Evaluates the signal fill an existing array */
    fun evaluateToBuffer(destination: FloatArray) {
        destination.indices.forEach { destination[it] = evaluateNext() }
    }
}

abstract class SignalCollection: Signal(), Collection<Signal>{
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

object SilentSignal: Signal() {
    override var period: Float = 1f

    override fun reset() { }
    override fun evaluateNext() = 0f

    override fun toString() = "SilentSignal"
}


class PeriodicSignal(
    frequency: Float,
    amp: Float = 1f,
    var waveShape: WaveShape = WaveShape.SINE
): Signal() {
    var frequency: Float = frequency
        set(value) {
            angularClock.frequency = value
            field = value
        }

    init {
        this.amp = amp
    }

    private val angularClock = AngularClock(frequency)

    override val period get() = SAMPLE_RATE / angularClock.frequency

    override fun reset() { this.angularClock.reset() }

    override fun evaluateNext(): Float =
        waveShape.lookupTable[angularClock.angle.toInt()] * amp
            .also { angularClock.tick() }

    override fun toString(): String {
        return "FuncSignal:" +
                "\n\tnote = ${angularClock.frequency} " +
                "\n\tamp  = $amp " +
                "\n\twaveShape = $waveShape"
    }
}

class HarmonicSignal(
    fundamental: Note,
    val harmonicSeries: HarmonicSeries = HarmonicSeries(),
    waveShape: WaveShape = WaveShape.SINE,
    amp: Float = 1f,
    autoNormalize: Boolean = true
): SignalCollection() {
    override val size get() = signals.size
    override val period = SAMPLE_RATE / fundamental.freq
    private val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(
            fundamental.freq*(i+1),
            0f,
            waveShape
        ).also { it.parents.add(this) }
    }
    var waveShape: WaveShape = waveShape
        set(value){
            signals.forEach{ it.waveShape = value }
            field = value
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
        if(autoNormalize)
            normalize()
        harmonicSeries.registerOnUpdatedCallback {
            for((overtone, amplitude) in harmonicSeries){
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
    signals: Collection<Signal>,
    amp: Float = 1f,
    autoNormalize: Boolean = true
) : SignalCollection(), MutableCollection<Signal> {
    constructor(vararg signal: Signal, amp: Float = 1f, autoNormalize: Boolean = true)
            : this(signal.toSet(), amp, autoNormalize)

    private val signals = mutableSetOf<Signal>()
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

    override val size: Int get() = signals.size
    override fun contains(element: Signal): Boolean = signals.contains(element)
    override fun containsAll(elements: Collection<Signal>): Boolean = signals.containsAll(elements)
    override fun isEmpty(): Boolean = signals.isEmpty()
    override fun retainAll(elements: Collection<Signal>): Boolean = this.signals.retainAll(elements).also { logd("hello") }
    override fun clear() {
        signals.forEach{ it.parents.remove(this) }
        signals.clear()
    }
    override fun iterator(): MutableIterator<Signal> = signals.iterator()
    override fun remove(element: Signal): Boolean {
        if(signals.contains(element)){
            element.parents.remove(this)
            signals.remove(element)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<Signal>): Boolean {
        var removed = false
        elements.forEach{
            if(this.remove(it))
                removed = true
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
        return added
    }


}

fun main() {
    val b = FloatArray(Constants.BUFFER_SIZE)
    val s = HarmonicSignal(Note.A_4, HarmonicSeries())
    printAvgTimeMillis(repeat = 50000){ s.evaluateToBuffer(b) }

}