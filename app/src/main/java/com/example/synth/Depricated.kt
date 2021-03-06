package com.example.synth

/** A recycling bin for this project. This code should not be used. */

/*
@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
val IntToByteArrayLookupTable = run{
    val table = mutableMapOf<Int, ByteArray>()
    for (i in Signal.MIN_16BIT_VALUE..Signal.MAX_16BIT_VALUE){
        table[i] = i.toBigInteger().toByteArray()
    }
    table
}

@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
fun List<Int>.toByteArray(): ByteArray {
    val byteArray = ByteArray(size * 2)
    var bIndex = 0
    for (i in indices) {
        val bytes = IntToByteArrayLookupTable[this[i]]
        if (bytes != null){
            if (bytes.size > 1)
                byteArray[bIndex] = bytes[1]
            byteArray[bIndex+1] = bytes[0]
        }
        bIndex += 2
    }
    return byteArray
}

@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
fun ByteArray.toList(bit: Int = 16): List<Int> =
    if(bit == 8) this.toList()
    else this
        .toList()
        .chunked(2)
        .map{ it[0] + it[1] }
        .toList()

@Deprecated("Use CircularShortArray instead")
private fun List<Int>.toShortArray() =
    this.map { it.toShort() }.toShortArray()

//fun List<Signal>.sum(): Signal{
//    if (size == 0) return Signal.NullSignal
//    val ret: Signal
//    with (measureTimeMillis {
//         ret = toSet().run{
//            if (this !in signalsToSumSignal)
//                signalsToSumSignal[this] = this.reduce { acc: Signal, sig: Signal -> acc + sig }
//
//            signalsToSumSignal[this]!!
//            }
//        }
//    ){ Log.d("m_time","$this milliseconds to sum $size notes") }
//
//    Log.d("m_mapSize", "signalsToSumSignal size: ${signalsToSumSignal.size}")
//    return ret
//}


class CircularIterator(val data: IntArray){
    private val index = CircularIndex(data.size)

    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularShortArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): IntArray {
        if (data.isEmpty()) throw Exception("cannot get chunk of array size 0")

        return IntArray(chunkSize){ data[index.getIndexAndIterate()] }
    }

    fun nextElement(): Int {
        if (data.isEmpty()) throw Exception("cannot get chunk of array size 0")

        return data[index.getIndexAndIterate()]
    }
}


/**
 * Circular Array of Shorts. This is ideal for Signals, as it means they can store just one period
 * of their wave and the AudioEngine can loop over it.
 */
class CircularShortArray {
    val size: Int
    private val data: ShortArray
    var index: CircularIndex

    constructor(size: Int){
        this.size = size
        this.data = ShortArray(size)
        this.index = CircularIndex(size)
    }

    constructor(data: ShortArray){
        this.size = data.size
        this.data = data
        this.index = CircularIndex(size)
    }


    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularShortArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): ShortArray {
        if (size == 0) throw Exception("cannot get chunk of array size 0")

        return ShortArray(chunkSize).also{ chunk ->
            for (i in chunk.indices) {
                chunk[i] = data[index.i]
                index.iterate()
            }
        }
    }
}

fun IntArray.toCircularShortArray() = CircularShortArray(this.toShortArray())



    /** Does the same as [nextChunk], but writes to an already existing array
     * @param array array that chunk is written to */
    fun writeNextChunkTo(array: IntArray, noiseAmount: Int = 0) {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in array.indices){
            array[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    /** Does the same as [writeNextChunkTo], but converts each value to a Short first
     * @param array array that chunk is written to */
    fun writeNextChunkAsShortArrayTo(array: ShortArray, noiseAmount: Int = 0) {
        data.normalize()
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in array.indices){
            array[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))].toShort()
        }
    }

    /** Does the same as [writeNextChunkTo], but converts each value to a Short first
     * @param destination array that chunk is written to */
    fun nextChunkAsShortArray(destination: ShortArray, noiseAmount: Int = 0) {
        data.normalize()
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))].toShort()
        }
    }
    fun IntArray.toShortArray(destination: ShortArray){
    if (destination.size == this.size){
        for(i in indices) {
            destination[i] = this[i].toShort()
        }
    }
}

*/

/*

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


/** Represents a sound, which can played by:
 * - to play once: use [android.media.AudioTrack]
 * - to play on a loop:
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
        val intervalSize = amps.map{ it.size }.lcm()
        CircularIntArray(intervalSize){
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
 */

/*
//----- List<Signal> ----- //

//fun List<Signal>.sum(): Signal{
//    val signalSet = this.toSet()
//    if(signalSet in signalsToSumSignal) return signalsToSumSignal[signalSet]!!
//    return when (size){
//        0 -> NullSignal
//        1 -> this[0]
//        else -> SumSignal(this.toSet())
//    }.also { signalsToSumSignal[signalSet] = it }
//}

fun List<Signal>.sum(): Signal{
    return when (size){
        0 -> NullSignal
        1 -> this[0]
        else -> SumSignal(this.toSet())
    }
}

    fun makeRandomSumSignal(): Signal{
        val numNotes = Random.nextInt(4,5)
        val notes = mutableSetOf<Signal>().apply {
            repeat(numNotes){
                add(SinSignal(Note.toList(5).random().freq))
            }
        }
        return SumSignal(notes)
    }

    fun makeRandomSignals(
        n: Int = Random.nextInt(10)
    ) = Array(n) { makeRandomSumSignal() }

 */

/*
package com.example.synth

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

class CircularFloatArray{
    companion object{
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767

        val sine = { i: Int, period: Int ->
            sin(TWO_PI * i / period).toFloat()
        }
        val cosine = { i: Int, period: Int ->
            cos(TWO_PI * i / period).toFloat()
        }

        fun signal(size: Int, func: (Int, Int) -> Float) =
            CircularFloatArray(size) { i -> func(i, size) }


        fun sinSignal(freq: Float, func: (Int, Int) -> Float) =
            signal(calculatePeriod(freq), sine)

        fun harmonicSignal(
            fundamental: Float,
            harmonics: Map<Int, Float>,
            func: (Int, Int) -> Float
        ): CircularFloatArray {
            val ret = sinSignal(fundamental, func)
            harmonics.filter { it.key != 1 }.forEach {
                ret += sinSignal(fundamental * it.key, func).apply{ volume = it.value }
            }
            return ret
        }

//        fun calculateCommonInterval(freqs: Set<Float>) =
//            freqs.map { calculatePeriod(it) }.lcm()

        fun calculatePeriod(freq: Float) = (AudioEngine.SAMPLE_RATE / freq).toInt()
    }

    var volume: Float = 1f
        set(value) {
            if (value in 0f..1f){
                data.normalize(value*-1, value)
                field = value
            }

        }
    val size: Int
        get() = data.size
    var noiseAmount: Int = 0
    var data: FloatArray
    private var index: CircularIndex

    constructor(size: Int, init: (Int) -> Float = {0f} ){
        this.data = FloatArray(size, init).also{ it.normalize() }
        this.index = CircularIndex(size)
    }

    constructor(data: FloatArray){
        this.data = data.also{ it.normalize() }
        this.index = CircularIndex(size)
    }

    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    fun normalize(){ data.normalize() }

    fun nextElement() = data[index.getIndexAndIterate()]

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularFloatArray's data to an IntArray of size [chunkSize].
     *
     *  The next time this method is called, its starting point will be where
     * the previous chunk ended.
     * @param chunkSize size of the returned IntArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): FloatArray {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        return FloatArray(chunkSize){ data[index.getIndexAndIterate(step, (noiseAmount > 0))] }
    }

    /** Does the same as [nextChunk], but writes to an existing array
     * @param destination array that chunk is written to */
    fun nextChunkTo(destination: FloatArray) {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    fun addValuesOfNextChunkTo(destination: FloatArray){
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] += data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    operator fun plusAssign(that: CircularFloatArray){
        for (i in this.data.indices) {
            this[i] += that.data[that.index.getIndexAndIterate(1, (noiseAmount > 0))]
        }
        this.normalize()
    }

    operator fun get(index: Int): Float{ return data[index] }
    operator fun set(index: Int, value: Float){ data[index] = value }

    override fun toString() = data.contentToString()


}

fun main() {
    val note = 261.63f
    val overtones = mapOf(
        1 to 1f,
        2 to 0.0f,
        6 to 0f
    )
    val signal = CircularFloatArray.harmonicSignal(note, overtones, CircularFloatArray.sine)
    print("interval: ${CircularFloatArray.calculatePeriod(note)} $signal")
}

 */

/*
        fun harmonicSignal(
            fundamental: Note,
            harmonics: Set<Int>,
            func: (Int, Int) -> Int
        ): CircularIntArray {
            val interval = calculatePeriod(fundamental.freq)
            val harmonicToPeriod = mutableMapOf<Int, Int>().apply {
                harmonics.forEach{ harmonic ->
                    this[harmonic] = interval/harmonic
                }
            }

            return CircularIntArray(interval){ i ->
                harmonics.fold(0){
                        sumAtIndex, harmonic -> sumAtIndex + func(i, harmonicToPeriod[harmonic]!!)
                }
            }
        }
 */


/*
package com.example.synth

import Signal
import SilentSignal
import com.example.signallib.HarmonicSeries
import com.example.signallib.signalCollections.HarmonicSignal
import com.example.signallib.Note
import com.example.signallib.Note.Companion.plus
import com.example.signallib.Note.Companion.toList
import com.example.signallib.WaveShape

class Oscillator{
    val harmonicSeries = HarmonicSeries()
    var amplitude = 0f
        set(value) {
            field = value
            onAmpChangedCallbacks.forEach { it.invoke(value) }
        }
    var bend = 0f
        set(value) {
            field = value
            for((_, signal) in noteToSignal){
                signal.bendAmount = this.bend
            }
            onBendChangedCallbacks.forEach { it.invoke(value) }
        }
    var waveShape: WaveShape = WaveShape.SINE
        set(value){
            for((_, signal) in noteToSignal){
                signal.waveShape = value
            }
            onWaveShapeChangedCallbacks.forEach { it.invoke(value) }
            field = value
        }

    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
    private val onWaveShapeChangedCallbacks = mutableSetOf<(WaveShape) -> Unit>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()
    private val onBendChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    init {
        assignSignalsToNotes()
    }


    fun bundleSignals() = mutableSetOf<Signal>()
        .apply{
            AppModel.pianoViewModel.pressedNotes.forEach{
                this += noteToSignal[it] ?: SilentSignal
        }
    }.toSet()


    fun registerOnWaveShapeChangedCallback(callback: (WaveShape) -> Unit){
        onWaveShapeChangedCallbacks.add(callback)
    }

   fun registerOnAmpChangedCallback(callback: (Float) -> Unit){
        onAmpChangedCallbacks.add(callback)
   }

    fun registerOnBendChangedCallback(callback: (Float) -> Unit){
        onBendChangedCallbacks.add(callback)
    }

    private fun assignSignalsToNotes(){
        noteToSignal.clear()
        AppModel.noteRange.toList().forEach {
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, waveShape, 1/7f)
        }
        val lastNote = AppModel.noteRange.endInclusive
        noteToSignal[lastNote+1] = HarmonicSignal(lastNote+1, harmonicSeries, waveShape,1/7f)
    }
}
 */


/*

//----- IntArray ----- //
/** Performs an in-place mapping of an IntArray*/
inline fun IntArray.mapInPlace(transform: (Int) -> Int){
    this.indices.forEach{ this[it] = transform(this[it]) }
}

inline fun IntArray.mapInPlaceIndexed(transform: (Int, Int) -> Int){
    this.indices.forEach{ this[it] = transform(it, this[it]) }
}

//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
//https://stackoverflow.com/questions/1226587/how-to-normalize-a-list-of-int-values
fun IntArray.normalize(
    lowerBound: Int = MIN_16BIT_VALUE,
    upperBound: Int = MAX_16BIT_VALUE
) {
    //Check that array isn't empty
    if (isEmpty()) return

    val minValue   = this.minByOrNull { it }!!
    val maxValue   = this.maxByOrNull { it }!!
    val valueRange = (maxValue - minValue).toFloat()
    val boundRange = (upperBound - lowerBound).toFloat()

    //Check that array isn't already normalized
    // (I would use in range, but this produces excess memory)
    if ((minValue == 0 && maxValue == 0)
        || (maxValue <= upperBound && maxValue > upperBound-2
                && minValue >= lowerBound && minValue < lowerBound+2)) {
        return
    }

    //Normalize
    for (i in indices) {
        this[i] = ( ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound ).toInt()
    }
}



fun IntArray.toShortArray(destination: ShortArray){
    if (this.size != destination.size)
        throw Exception("Cannot clone to array of different size")
    for (i in destination.indices){
        destination[i] = this[i].toShort()
    }
}

fun FloatArray.toShortIntArray(destination: ShortArray, scalar: Int){
    if (this.size != destination.size)
        throw Exception("Cannot clone to array of different size")
    for (i in destination.indices){
        destination[i] = (this[i] * scalar).toInt().toShort()
    }
}
 */


