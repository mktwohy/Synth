package com.example.synth

class Clock(
    frequency: Float,
    initAngle: Float = 0f
) {
    var period = 0f
    var frequencyBend = 1f
    var frequency: Float = 0f
        set(value){
            field = value
            period = Constants.SAMPLE_RATE / this.frequency
        }
    var angle = initAngle

    init {
        this.frequency = frequency
        this.angle = initAngle
    }

    fun tick(){
        angle = (angle + (2f / period*frequencyBend) ) % 2f
    }

    fun sync(that: Clock){
        this.angle = that.angle
    }

    fun reset(){
        this.angle = 0f
    }
}

fun main(){
    val clock = Clock(440f)
    repeat(103){ clock.tick() }
    println(clock.period)
    clock.frequency = 100f
    println(clock.period)

}