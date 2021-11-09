import com.example.signallib.*
import com.example.signallib.Constants.SAMPLE_RATE
import com.example.signallib.Note.Companion.bend

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal(
    var sampleRate: Int = SAMPLE_RATE){
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

    override val period get() = sampleRate / angularClock.frequency

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

