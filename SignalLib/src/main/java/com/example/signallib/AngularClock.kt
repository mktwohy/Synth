package com.example.signallib

/** Used by [Signal] to keep track of its current angle with respect to some frequency
 *
 * It assumes that [tick] is being called every single audio sample.
 * ie: if the sample rate is 44100, the [angle] is being updated 44100 times per second,
 * regardless of its [frequency]
 */
class AngularClock(
    frequency: Float,
    initAngle: Float = 0f
){
    var frequency: Float = 0f
        set(value){
            tickAmount = 360 / (com.example.signallib.Constants.SAMPLE_RATE / value)
            field = value
        }
    var tickAmount: Float = 0f
    var angle = 0f
    var backupAngle = 0f //used during save and restore

    init {
        this.frequency = frequency
        this.angle = initAngle
    }

    /** essentially an update function. It iterates the current angle according to its frequency */
    fun tick(){
        angle = (angle + tickAmount) % 360
    }

    fun reset(){
        this.angle = 0f
    }

    fun sync(that: AngularClock){
        this.angle = that.angle
    }

    fun save(){ backupAngle = angle }
    fun restore(){ angle = backupAngle }
}