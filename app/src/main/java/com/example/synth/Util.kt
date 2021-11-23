package com.example.synth


import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.signallib.Note
import java.lang.StringBuilder
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

//----- Color -----//
fun Color.mix(that: Color) =
    Color(
        this.red/2 + that.red/2,
        this.green/2 + that.green/2,
        this.blue/2 + that.blue/2,
        this.alpha/2 + that.alpha/2
    )

operator fun Color.plus(that: Color) = this.mix(that)


fun Note.color() =
    if(this.name[1] == '_') Color.White else Color.Black

fun Note.color(isPressed: Boolean) =
    if(isPressed) this.color() + Color(0.4f, 0.0f, 1f, 0.5f)
    else this.color()


fun logd(message: Any){ Log.d("m_tag",message.toString()) }

fun logTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { logd("$title $it ms") }
}
fun printTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { println("$title $it ms") }
}
fun printAvgTimeMillis(title: String = "", repeat: Int = 100, block: () -> Unit){
    println("$title avg: ${avgTimeMillis(repeat, block)}")
}
fun avgTimeMillis(repeat: Int, block: () -> Unit): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureTimeMillis{ block() }
            .also{ times += it }
    }
    return times.average()
}
fun avgTimeNano(repeat: Int, block: () -> Any?): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureNanoTime{ block() }
            .also{ times += it }
    }
    return times.average()
}




