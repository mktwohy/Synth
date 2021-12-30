package com.example.signallib.enums

enum class HarmonicFilter(val function: (Int) -> Boolean){
    FUNDAMENTAL ({ i: Int -> i == 1 }),
    ODD         ({ i: Int -> i % 2 != 0 }),
    EVEN        ({ i: Int -> i % 2 == 0 }),
    ALL         ({ true }),
    NONE        ({ false });
}