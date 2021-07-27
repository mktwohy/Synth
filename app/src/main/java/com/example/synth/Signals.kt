package com.example.synth

import kotlin.math.PI
import kotlin.math.sin

/**
 * @property amplitudes represents the audio data.
 * @property frequencies the known frequencies in the Signal
 */
interface SignalProperties{
    val amplitudes: CircularIntArray
    val frequencies: MutableSet<Int>
}


/** Represents a sound, who can played two ways:
 * - to play on a loop, set [AudioEngine.signalForPlayback] = *Signal you want to play*
 * - to play once, write a larger chunk of Signal's data (using [Signal.amplitudes]'s nextChunk())
 * to an [android.media.AudioTrack] and call play()
 * */
abstract class Signal: SignalProperties{
    companion object{
        const val SAMPLE_RATE       = AudioEngine.SAMPLE_RATE
        const val BUFFER_SIZE       = AudioEngine.BUFFER_SIZE
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767
    }

    abstract fun transpose(step: Int): Signal

    override fun toString(): String{
        val s = StringBuilder()
        for(value in amplitudes){
            s.append(value)
            s.append(" ")
        }
        return s.toString()
    }
}


/** Represents a silent signal of size [AudioEngine.BUFFER_SIZE] */
object NullSignal: Signal() {
    override val frequencies = mutableSetOf<Int>()
    override val amplitudes = CircularIntArray(BUFFER_SIZE)
    override fun transpose(step: Int) = NullSignal
}


/** Represents a pure sine wave */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = mutableSetOf(freq)
    override val amplitudes = run{
        val period = SAMPLE_RATE / freq
        CircularIntArray(period) { i -> (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt() }
    }

    override fun transpose(step: Int): Signal {
        val fundFreq = frequencies.minByOrNull { it }
            ?: return NullSignal
        return SinSignal( (fundFreq * Interval.stepToRatio(step)) .toInt() )
    }
}


/** Combines two or more Signals into one Signal. */
class SumSignal(signals: Set<Signal>): Signal() {
    override val frequencies = mutableSetOf<Int>().apply {
        for(s in signals){
            addAll(s.frequencies)
        }
    }
    override val amplitudes = run{
        val amps = signals.map { it.amplitudes }
        CircularIntArray(amps.map{ it.size }.lcm()){
            amps.fold(0){ sumAtIndex, circIntArr -> sumAtIndex + circIntArr.nextElement() }
        }

    }

    override fun transpose(step: Int): Signal {
        val ratio = Interval.stepToRatio(step)
        val transposedSignals = mutableListOf<Signal>()
        for(f in frequencies){
            transposedSignals.add(SinSignal(f * ratio.toInt()))
        }
        return transposedSignals.sum()
    }
}