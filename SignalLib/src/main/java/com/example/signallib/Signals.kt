import com.example.signallib.*
import kotlin.math.sign

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal(
    val signalSettings: SignalSettings
){
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

class PeriodicSignal(
    frequency: Float,
    amp: Float = 1f,
    signalSettings: SignalSettings
): Signal(signalSettings) {
    var frequency: Float = frequency
        set(value) {
            angularClock.frequency = value
            field = value
        }

    init {
        this.amp = amp
    }

    private val angularClock = AngularClock(frequency, signalSettings.sampleRate.value)

    override val period get() = signalSettings.sampleRate.value / angularClock.frequency

    override fun reset() { this.angularClock.reset() }

    override fun evaluateNext(): Float =
        signalSettings.waveShape.value.lookupTable[angularClock.angle.toInt()] * amp
            .also { angularClock.tick() }

    override fun toString(): String {
        return "FuncSignal:" +
                "\n\tnote = ${angularClock.frequency} " +
                "\n\tamp  = $amp " +
                "\n\twaveShape = ${signalSettings.waveShape}"
    }
}

