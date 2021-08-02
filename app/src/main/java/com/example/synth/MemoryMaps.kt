package com.example.synth

/** Maps for storing computed data.
 * This reduces the amount of excess computation and duplicate object creation */

/** Stores recently played SumSignals, which are typically computed by List<Signal>.sum() */
val signalsToSumSignal = mutableMapOf<Set<Signal>, Signal>()

val numsToLcd = mutableMapOf<Set<Int>, Int>()
