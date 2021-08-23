package com.example.synth

import android.graphics.Paint
import android.util.Rational
import androidx.compose.ui.graphics.Color
import java.util.*
import kotlin.math.PI

object Constants{
    const val PI               = Math.PI.toFloat()
    const val TWO_PI           = 2.0 * PI.toFloat()
    const val MIN_16BIT_VALUE  = -32_768
    const val MAX_16BIT_VALUE  = 32_767
    const val NUM_HARMONICS    = 15
    const val SAMPLE_RATE      = 44100
    const val BUFFER_SIZE      = 512
}

/** The [Paint]s used in the UI */
enum class Paints(val paint: Paint){
    WHITE   ( Paint().apply { setARGB(255, 255, 255, 255) } ),
    PURPLE  ( Paint().apply { setARGB(100, 255, 0, 255); strokeWidth = 4f } ),
    BLACK   ( Paint().apply { setARGB(255, 0, 0, 0); strokeWidth = 2f } );
}

/** Musical intervals and their associated mathematical ratio */
enum class Interval(val ratio: Rational){
    PER_1   (Rational(1, 1)),
    MIN_2   (Rational(16, 15)),
    MAJ_2   (Rational(9, 8)),
    MIN_3   (Rational(6, 5)),
    MAJ_3   (Rational(5, 4)),
    PER_4   (Rational(4, 3)),
    TRITONE (Rational(45, 32)),
    PER_5   (Rational(3,2)),
    MIN_6   (Rational(8,5)),
    MIN_7   (Rational(9,5)),
    MAJ_7   (Rational(15,8)),
    OCTAVE  (Rational(2,1));
}


/**
 * Every note on a piano and the frequency of its fundamental sine wave
 *
 * Naming format:
 *
 * - First character:  note name
 * - Second character: natural (_), sharp (s), or flat (f)
 *      -(Unfortunately, I couldn't use # for sharp, as it is a literal)
 *  - Third character:  octave
 *
 *
 * Enums are generated by a python script and https://pages.mtu.edu/~suits/notefreqs.html
 */
enum class Note(val freq: Float) {
    C_0 (16.35f),
    Cs0 (17.32f),
    D_0 (18.35f),
    Ds0 (19.45f),
    E_0 (20.6f),
    F_0 (21.83f),
    Fs0 (23.12f),
    G_0 (24.5f),
    Gs0 (25.96f),
    A_0 (27.5f),
    As0 (29.14f),
    B_0 (30.87f),
    C_1 (32.7f),
    Cs1 (34.65f),
    D_1 (36.71f),
    Ds1 (38.89f),
    E_1 (41.2f),
    F_1 (43.65f),
    Fs1 (46.25f),
    G_1 (49.0f),
    Gs1 (51.91f),
    A_1 (55.0f),
    As1 (58.27f),
    B_1 (61.74f),
    C_2 (65.41f),
    Cs2 (69.3f),
    D_2 (73.42f),
    Ds2 (77.78f),
    E_2 (82.41f),
    F_2 (87.31f),
    Fs2 (92.5f),
    G_2 (98.0f),
    Gs2 (103.83f),
    A_2 (110.0f),
    As2 (116.54f),
    B_2 (123.47f),
    C_3 (130.81f),
    Cs3 (138.59f),
    D_3 (146.83f),
    Ds3 (155.56f),
    E_3 (164.81f),
    F_3 (174.61f),
    Fs3 (185.0f),
    G_3 (196.0f),
    Gs3 (207.65f),
    A_3 (220.0f),
    As3 (233.08f),
    B_3 (246.94f),
    C_4 (261.63f),
    Cs4 (277.18f),
    D_4 (293.66f),
    Ds4 (311.13f),
    E_4 (329.63f),
    F_4 (349.23f),
    Fs4 (369.99f),
    G_4 (392.0f),
    Gs4 (415.3f),
    A_4 (440.0f),
    As4 (466.16f),
    B_4 (493.88f),
    C_5 (523.25f),
    Cs5 (554.37f),
    D_5 (587.33f),
    Ds5 (622.25f),
    E_5 (659.25f),
    F_5 (698.46f),
    Fs5 (739.99f),
    G_5 (783.99f),
    Gs5 (830.61f),
    A_5 (880.0f),
    As5 (932.33f),
    B_5 (987.77f),
    C_6 (1046.5f),
    Cs6 (1108.73f),
    D_6 (1174.66f),
    Ds6 (1244.51f),
    E_6 (1318.51f),
    F_6 (1396.91f),
    Fs6 (1479.98f),
    G_6 (1567.98f),
    Gs6 (1661.22f),
    A_6 (1760.0f),
    As6 (1864.66f),
    B_6 (1975.53f),
    C_7 (2093.0f),
    Cs7 (2217.46f),
    D_7 (2349.32f),
    Ds7 (2489.02f),
    E_7 (2637.02f),
    F_7 (2793.83f),
    Fs7 (2959.96f),
    G_7 (3135.96f),
    Gs7 (3322.44f),
    A_7 (3520.0f),
    As7 (3729.31f),
    B_7 (3951.07f),
    C_8 (4186.01f),
    Cs8 (4434.92f),
    D_8 (4698.63f),
    Ds8 (4978.03f),
    E_8 (5274.04f),
    F_8 (5587.65f),
    Fs8 (5919.91f),
    G_8 (6271.93f),
    Gs8 (6644.88f),
    A_8 (7040.0f),
    As8 (7458.62f),
    B_8 (7902.13f);


    companion object{
        //saving this as a static, immutable variable ensures that the memory is only allocated once,
        //which makes methods like [transpose] and [toList] more memory efficient
        private val notes = values().toList()

        fun Note.color() =
            if(this.name[1] == '_') Color.White else Color.Black

        fun Note.color(isPressed: Boolean) =
            if(isPressed) this.color() + Color(0.4f, 0.0f, 1f, 0.5f)
            else this.color()


        fun toList() = notes

        fun toList(start: Note, end: Note) =
            EnumSet.range(start, end).toList()

        fun toList(octave: Int) =
            if (octave in 0..8) notes.subList(octave*12, (octave+1)*12)
            else listOf()

        fun random() =
            notes.random()

        fun Note.transpose(steps: Int) =
            notes[notes.indexOf(this) + steps]

        operator fun Note.plus(steps: Int) = transpose(steps)
        operator fun Note.minus(steps: Int) = transpose(-1*steps)
    }
}