package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.random.Random

/** Uses an AudioTrack object to play its current PCM audio data on a loop. */
class AudioEngine(private val pianoView: PianoView){
    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 256
    }

    var noiseAmount = 0
    private var currentAudio: CircularIntArray = NullSignal.audio
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



    fun start(){
        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING){
            runMainLoop = true
            mainLoop()
        }
    }

    fun stop(){ runMainLoop = false }

    fun mute(){ currentAudio = NullSignal.audio }

    fun updatePcm(pressedKeys: Set<Key>){
        currentAudio = pressedKeys
            .map { it.signal }
            .sum()
            .audio.also { it.normalize() }
    }

    fun destroyAudioTrack(){
        audioTrack.flush()
        audioTrack.release()
    }

    private fun mainLoop(){
        Thread {
            var chunk: IntArray
            audioTrack.play()
            while (runMainLoop) {
                chunk = currentAudio.nextChunk(BUFFER_SIZE, noiseAmount)
//                Log.d("m_pcm", chunk.toList().toString())
                audioTrack.write(chunk.toShortArray(), 0, chunk.size)
                Log.d("m_audioTrack","position: ${audioTrack.playbackHeadPosition}")
                pianoView.postInvalidate()
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}