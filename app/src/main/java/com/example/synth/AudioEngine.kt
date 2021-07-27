package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * A wrapper class for [AudioTrack].
 *
 * At any time, set [signalForPlayback] to select the audio you want to play and then call
 * [start] to begin playing this audio on a loop.
 *
 * @property noiseAmount Controls the amount of harmonic noise applied to currentAudio
 * The noise is a result of manipulating [CircularIntArray.nextChunk]
 * @property signalForPlayback the audio to be played on a loop by the [AudioEngine]
 */
class AudioEngine{
    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 256
    }

    var noiseAmount = 0
    var signalForPlayback: Signal = NullSignal
        set(audio) {
            field = audio.also{ it.amplitudes.normalize() }
        }
    private var runMainLoop = false
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    /** Begins playing *currentAudio* on a loop */
    fun start(){
        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING){
            runMainLoop = true
            mainLoop()
        }
    }

    fun stop(){ runMainLoop = false }

    fun mute(){ signalForPlayback = NullSignal }

    fun destroyAudioTrack(){
        audioTrack.flush()
        audioTrack.release()
    }

    private fun mainLoop(){
        Thread {
            var chunk: IntArray
            audioTrack.play()
            while (runMainLoop) {
                chunk = signalForPlayback.amplitudes.nextChunk(BUFFER_SIZE, noiseAmount)

                Log.d("m_pcm", chunk.toList().toString())
                audioTrack.write(chunk.toShortArray(), 0, chunk.size)
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}