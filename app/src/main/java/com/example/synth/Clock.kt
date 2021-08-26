package com.example.synth

class Clock(
    frequency: Float,
    initAngle: Float = 0f
) {
    var period = 0f
    var frequency: Float = 0f
        set(value){
            period = Constants.SAMPLE_RATE / value
            field = value
        }
    var angle = initAngle
    var backupAngle = 0f

    init {
        this.frequency = frequency
        this.angle = initAngle
    }

    fun tick(){
        angle = (angle + (2f / period) ) % 2f
    }

    fun sync(that: Clock){
        this.angle = that.angle
    }

    fun reset(){
        this.angle = 0f
    }

    fun save(){ backupAngle = angle }

    fun restore(){ angle = backupAngle }
}

fun main(){
    val clock = Clock(440f)
    repeat(103){ clock.tick() }
    println(clock.period)
    clock.frequency = 100f
    println(clock.period)

}