package com.example.synth

class AngularClock(
    frequency: Float,
    initAngle: Float = 0f
){
    var frequency: Float = 0f
        set(value){
            tickAmount = 360 / (Constants.SAMPLE_RATE / value)
            field = value
        }
    var tickAmount: Float = 0f
    var angle = 0f
    var backupAngle = 0f //used during save and restore

    init {
        this.frequency = frequency
        this.angle = initAngle
    }

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