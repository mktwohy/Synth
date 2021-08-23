package com.example.synth

class Clock(
    var frequency: Float,
    initAngle: Float = 0f
) {
    var angle = initAngle

    fun tick(){
        angle = (angle + 2f / (Constants.SAMPLE_RATE / this.frequency)) % 2f
    }

    fun sync(that: Clock){
        this.angle = that.angle
    }

    fun reset(){
        this.angle = 0f
    }
}