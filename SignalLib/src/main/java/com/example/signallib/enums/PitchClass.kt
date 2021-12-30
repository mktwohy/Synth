package com.example.signallib.enums

enum class PitchClass {
    C, Cs, D, Ds, E, F, Fs, G, Gs, A, As, B;

    companion object{
        val classes = values().toList()
    }
}