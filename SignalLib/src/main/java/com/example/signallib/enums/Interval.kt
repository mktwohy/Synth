package com.example.signallib.enums

import android.util.Rational

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