package com.example.signallib.enums

import android.util.Rational

/** Musical intervals and their associated mathematical ratio */
enum class Interval(val ratio: Float){
    PER_1   (1f/1f),
    MIN_2   (16f / 15f),
    MAJ_2   (9f / 8f),
    MIN_3   (6f / 5f),
    MAJ_3   (5f / 4f),
    PER_4   (4f / 3f),
    TRITONE (45f / 32f),
    PER_5   (3f /2f),
    MIN_6   (8f /5f),
    MIN_7   (9f /5f),
    MAJ_7   (15f /8f),
    OCTAVE  (2f /1f);
}