package com.lift.bro.audio

import android.content.Context
import android.speech.tts.TextToSpeech

class AndroidAudioPlayer(context: Context): AudioPlayer {

    private val tts: TextToSpeech by lazy {
        TextToSpeech(context) {
        }
    }

    override fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun stop() {
        tts.stop()
    }
}
