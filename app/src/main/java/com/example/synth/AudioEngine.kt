package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * A wrapper class for [AudioTrack] that plays a [Signal]'s audio on a loop.
 *
 *
 * Example Usage:
 * ```
 * val signal = SinSignal(Note.C_4.freq)    //create an instance of Signal
 * val audioEngine = AudioEngine()          //create an instance of AudioEngine
 * audioEngine.signalForPlayback = signal   //set signalForPlayback to signal you want to play
 * audioEngine.start()                      //start engine
 * ...
 * audioEngine.mute()                       //mute the audio
 * ...
 * audioEngine.signalForPlayback = SinSignal(440) //play a new Signal mid-playback
 * ...
 * audioEngine.stop()
 * ```
 * @property noiseAmount Controls the amount of harmonic noise applied to currentAudio
 * The noise is a result of manipulating [CircularIntArray.nextChunk]
 * @property currentAudio the audio to be played on a loop by the [AudioEngine]
 */
class AudioEngine{
    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 512
    }

    var currentAudio = setOf<CircularIntArray>()
        set(newAudio) {
            field =
                if(newAudio.isEmpty()) setOf()
                else newAudio
        }
    var noiseAmount = 0
    private var runMainLoop = false
    private val buffer = IntArray(BUFFER_SIZE)
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

    fun destroyAudioTrack(){
        audioTrack.flush()
        audioTrack.release()
    }

    private fun mainLoop(){
        Thread {
            audioTrack.play()
            while (runMainLoop) {
                if(currentAudio.isEmpty()){
                    buffer.indices.forEach{ i -> buffer[i] = 0 }
                }else{
                    for(audio in currentAudio){
                        audio.addValuesOfNextChunkTo(buffer)
                    }
                }

                buffer.normalize()
                Log.d("m_audio","${buffer.contentToString()}")
                audioTrack.write(buffer.toShortArray(), 0, BUFFER_SIZE)
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}