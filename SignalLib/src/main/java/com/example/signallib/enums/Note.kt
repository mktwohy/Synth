package com.example.signallib.enums

import java.util.*
import kotlin.math.abs
import com.example.signallib.enums.PitchClass.*

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
enum class Note(
    val freq: Float,
    val pitchClass: PitchClass,
    val natural: Boolean,
    val octave: Int
): Comparable<Note> {
    C_0 (16.35f, C, true, 0),
    Cs0 (17.32f, Cs, false, 0),
    D_0 (18.35f, D, true, 0),
    Ds0 (19.45f, Ds, false, 0),
    E_0 (20.6f, E, true, 0),
    F_0 (21.83f, F, true, 0),
    Fs0 (23.12f, Fs, false, 0),
    G_0 (24.5f, G, true, 0),
    Gs0 (25.96f, Gs, false, 0),
    A_0 (27.5f, A, true, 0),
    As0 (29.14f, As, false, 0),
    B_0 (30.87f, B, true, 0),
    C_1 (32.7f, C, true, 1),
    Cs1 (34.65f, Cs, false, 1),
    D_1 (36.71f, D, true, 1),
    Ds1 (38.89f, Ds, false, 1),
    E_1 (41.2f, E, true, 1),
    F_1 (43.65f, F, true, 1),
    Fs1 (46.25f, Fs, false, 1),
    G_1 (49.0f, G, true, 1),
    Gs1 (51.91f, Gs, false, 1),
    A_1 (55.0f, A, true, 1),
    As1 (58.27f, As, false, 1),
    B_1 (61.74f, B, true, 1),
    C_2 (65.41f, C, true, 2),
    Cs2 (69.3f, Cs, false, 2),
    D_2 (73.42f, D, true, 2),
    Ds2 (77.78f, Ds, false, 2),
    E_2 (82.41f, E, true, 2),
    F_2 (87.31f, F, true, 2),
    Fs2 (92.5f, Fs, false, 2),
    G_2 (98.0f, G, true, 2),
    Gs2 (103.83f, Gs, false, 2),
    A_2 (110.0f, A, true, 2),
    As2 (116.54f, As, false, 2),
    B_2 (123.47f, B, true, 2),
    C_3 (130.81f, C, true, 3),
    Cs3 (138.59f, Cs, false, 3),
    D_3 (146.83f, D, true, 3),
    Ds3 (155.56f, Ds, false, 3),
    E_3 (164.81f, E, true, 3),
    F_3 (174.61f, F, true, 3),
    Fs3 (185.0f, Fs, false, 3),
    G_3 (196.0f, G, true, 3),
    Gs3 (207.65f, Gs, false, 3),
    A_3 (220.0f, A, true, 3),
    As3 (233.08f, As, false, 3),
    B_3 (246.94f, B, true, 3),
    C_4 (261.63f, C, true, 4),
    Cs4 (277.18f, Cs, false, 4),
    D_4 (293.66f, D, true, 4),
    Ds4 (311.13f, Ds, false, 4),
    E_4 (329.63f, E, true, 4),
    F_4 (349.23f, F, true, 4),
    Fs4 (369.99f, Fs, false, 4),
    G_4 (392.0f, G, true, 4),
    Gs4 (415.3f, Gs, false, 4),
    A_4 (440.0f, A, true, 4),
    As4 (466.16f, As, false, 4),
    B_4 (493.88f, B, true, 4),
    C_5 (523.25f, C, true, 5),
    Cs5 (554.37f, Cs, false, 5),
    D_5 (587.33f, D, true, 5),
    Ds5 (622.25f, Ds, false, 5),
    E_5 (659.26f, E, true, 5),
    F_5 (698.46f, F, true, 5),
    Fs5 (739.99f, Fs, false, 5),
    G_5 (783.99f, G, true, 5),
    Gs5 (830.61f, Gs, false, 5),
    A_5 (880.0f, A, true, 5),
    As5 (932.33f, As, false, 5),
    B_5 (987.77f, B, true, 5),
    C_6 (1046.5f, C, true, 6),
    Cs6 (1108.73f, Cs, false, 6),
    D_6 (1174.66f, D, true, 6),
    Ds6 (1244.51f, Ds, false, 6),
    E_6 (1318.51f, E, true, 6),
    F_6 (1396.91f, F, true, 6),
    Fs6 (1479.98f, Fs, false, 6),
    G_6 (1567.98f, G, true, 6),
    Gs6 (1661.22f, Gs, false, 6),
    A_6 (1760.0f, A, true, 6),
    As6 (1864.66f, As, false, 6),
    B_6 (1975.53f, B, true, 6),
    C_7 (2093.0f, C, true, 7),
    Cs7 (2217.46f, Cs, false, 7),
    D_7 (2349.32f, D, true, 7),
    Ds7 (2489.02f, Ds, false, 7),
    E_7 (2637.02f, E, true, 7),
    F_7 (2793.83f, F, true, 7),
    Fs7 (2959.96f, Fs, false, 7),
    G_7 (3135.96f, G, true, 7),
    Gs7 (3322.44f, Gs, false, 7),
    A_7 (3520.0f, A, true, 7),
    As7 (3729.31f, As, false, 7),
    B_7 (3951.07f, B, true, 7),
    C_8 (4186.01f, C, true, 8),
    Cs8 (4434.92f, Cs, false, 8),
    D_8 (4698.64f, D, true, 8),
    Ds8 (4978.03f, Ds, false, 8),
    E_8 (5274.04f, E, true, 8),
    F_8 (5587.65f, F, true, 8),
    Fs8 (5919.91f, Fs, false, 8),
    G_8 (6271.93f, G, true, 8),
    Gs8 (6644.88f, Gs, false, 8),
    A_8 (7040.0f, A, true, 8),
    As8 (7458.62f, As, false, 8),
    B_8 (7902.13f, B, true, 8);

    companion object{
        //saving this as a static, immutable variable ensures that the memory is only allocated once,
        //which makes methods like [transpose] and [toList] more memory efficient
        val notes = values().toList()

        fun toList() = notes

        fun toList(start: Note, end: Note) =
            EnumSet.range(start, end).toList()

        fun ClosedRange<Note>.toList() =
            toList(this.start, this.endInclusive)

        fun toList(octave: Int) =
            if (octave in 0..8) notes.subList(octave*12, (octave+1)*12)
            else listOf()

        fun random() =
            notes.random()

        fun Note.transpose(steps: Int): Note =
            if(this == C_0 || this == B_8)
                this
            else
                notes[notes.indexOf(this) + steps]

        fun Note.bend(bendAmount: Float): Float{
            val sign = if(bendAmount < 0f) -1 else 1
            val stepBendAmount = bendAmount.toInt()
            val fractionalBendAmount = abs(bendAmount - stepBendAmount)
            val transposeNote = this.transpose(stepBendAmount)
            val freqBend = ((transposeNote+1).freq - transposeNote.freq) * fractionalBendAmount * sign
            return transposeNote.freq + freqBend
        }

        fun Note.nextWhiteNote(): Note{
            val n = this + 1
            return if (n.natural) n else n + 1
        }

        fun Note.prevWhiteNote(): Note{
            val n = this - 1
            return if (n.natural) n else n - 1
        }

        operator fun Note.plus(steps: Int) = transpose(steps)
        operator fun Note.minus(steps: Int) = transpose(-1*steps)
    }
}