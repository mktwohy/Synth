package com.example.synth


enum class Interval(val ratio: Float){
    PER_1   (1/1    .toFloat()),
    MIN_2   (16/15  .toFloat()),
    MAJ_2   (9/8    .toFloat()),
    MIN_3   (6/5    .toFloat()),
    MAJ_3   (5/4    .toFloat()),
    PER_4   (4/3    .toFloat()),
    TRITONE (45/32  .toFloat()),
    PER_5   (3/2    .toFloat()),
    MIN_6   (8/5    .toFloat()),
    MIN_7   (9/5    .toFloat()),
    MAJ_7   (15/8   .toFloat()),
    OCTAVE  (2/1    .toFloat()),
}

/**
 * Enum naming format:
 *
 *      First character:  note name
 *      Second character: natural (_), sharp (s), or flat (f)
 *          (Unfortunately, I couldn't use # for sharp, as it is a literal)
 *      Third character:  octave
 */
enum class Note(val freq: Int) {
    C_0 (16),
    Cs0 (17),
    D_0 (18),
    Ds0 (19),
    E_0 (21),
    F_0 (22),
    Fs0 (23),
    G_0 (24),
    Gs0 (26),
    A_0 (28),
    As0 (29),
    B_0 (31),
    C_1 (33),
    Cs1 (35),
    D_1 (37),
    Ds1 (39),
    E_1 (41),
    F_1 (44),
    Fs1 (46),
    G_1 (49),
    Gs1 (52),
    A_1 (55),
    As1 (58),
    B_1 (62),
    C_2 (65),
    Cs2 (69),
    D_2 (73),
    Ds2 (78),
    E_2 (82),
    F_2 (87),
    Fs2 (92),
    G_2 (98),
    Gs2 (104),
    A_2 (110),
    As2 (117),
    B_2 (123),
    C_3 (131),
    Cs3 (139),
    D_3 (147),
    Ds3 (156),
    E_3 (165),
    F_3 (175),
    Fs3 (185),
    G_3 (196),
    Gs3 (208),
    A_3 (220),
    As3 (233),
    B_3 (247),
    C_4 (262),
    Cs4 (277),
    D_4 (294),
    Ds4 (311),
    E_4 (330),
    F_4 (349),
    Fs4 (370),
    G_4 (392),
    Gs4 (415),
    A_4 (440),
    As4 (466),
    B_4 (494),
    C_5 (523),
    Cs5 (554),
    D_5 (587),
    Ds5 (622),
    E_5 (659),
    F_5 (698),
    Fs5 (740),
    G_5 (784),
    Gs5 (831),
    A_5 (880),
    As5 (932),
    B_5 (988),
    C_6 (1046),
    Cs6 (1109),
    D_6 (1175),
    Ds6 (1245),
    E_6 (1319),
    F_6 (1397),
    Fs6 (1480),
    G_6 (1568),
    Gs6 (1661),
    A_6 (1760),
    As6 (1865),
    B_6 (1976),
    C_7 (2093),
    Cs7 (2217),
    D_7 (2349),
    Ds7 (2489),
    E_7 (2637),
    F_7 (2794),
    Fs7 (2960),
    G_7 (3136),
    Gs7 (3322),
    A_7 (3520),
    As7 (3729),
    B_7 (3951),
    C_8 (4186),
    Cs8 (4435),
    D_8 (4699),
    Ds8 (4978),
    E_8 (5274),
    F_8 (5588),
    Fs8 (5920),
    G_8 (6272),
    Gs8 (6645),
    A_8 (7040),
    As8 (7459),
    B_8 (7902);

    companion object{
        fun toList(noteRange: IntRange) =
            values().toList().subList(noteRange.first, noteRange.last+1)

        fun toList(octave: Int) =
            if (octave > 8) listOf<Note>()
            else toList(octave*12 until ((octave+1) * 12) )

        fun random() =
            values().random()
    }
}