package com.example.synth


import androidx.compose.ui.graphics.Color
import com.example.signallib.Note

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
