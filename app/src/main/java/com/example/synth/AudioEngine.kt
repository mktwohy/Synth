package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/** Uses an AudioTrack object to play its current PCM audio data on a loop. */
class AudioEngine(private val pianoView: PianoView){
    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 512
    }

    private var currentAudio: CircularIntArray = Signal.NullSignal.audio
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
    val isPlaying
        get() = audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING

    fun start(){
        if (!isPlaying){
            runMainLoop = true
            mainLoop()
        }
    }

    fun stop(){ runMainLoop = false }

    fun mute(){ currentAudio = Signal.NullSignal.audio }

    fun updatePcm(pressedKeys: Set<Key>){
        currentAudio = pressedKeys
            .map { it.signal }
            .sum()
            .audio.also { it.normalize() }
    }

    private fun mainLoop(){
        Thread {
            audioTrack.play()
            var chunk: ShortArray
            while (runMainLoop) {
                chunk = currentAudio.nextChunk(BUFFER_SIZE).toShortArray()
                Log.d("m_pcm", chunk.toList().toString())
                audioTrack.write(chunk, 0, chunk.size)
                pianoView.postInvalidate()
            }
            audioTrack.stop()
        }.start()
    }
}