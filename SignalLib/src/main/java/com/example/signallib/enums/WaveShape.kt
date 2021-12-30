package com.example.signallib.enums


const val WAVEFORM_SIZE = 360
enum class WaveShape(
    val lookupTable: FloatArray,
    val abbreviation: String
) {
    SINE(
        FloatArray(WAVEFORM_SIZE) {
            kotlin.math.sin(com.example.signallib.degreeToRadian(it.toFloat()))
        },
        "SIN"
    ),
    TRIANGLE(
        FloatArray(WAVEFORM_SIZE){
            val amp = 1f
            val period = WAVEFORM_SIZE
            (4*amp/period) * kotlin.math.abs(((it - (period / 4)).mod(period)) - (period / 2)) - amp
        },
        "TRI"
    ),
    SAWTOOTH(
        FloatArray(WAVEFORM_SIZE) {
            (2 * it.toFloat() / WAVEFORM_SIZE) - 1f
        },
        "SAW"
    ),
    SQUARE(
        FloatArray(WAVEFORM_SIZE){
            if(it < WAVEFORM_SIZE / 2) 1f else -1f
        },
        "SQR"
    ),
//    NOISE(  FloatArray(WAVEFORM_SIZE){
//        (-100..100).random() / 100f
//    } ),
}