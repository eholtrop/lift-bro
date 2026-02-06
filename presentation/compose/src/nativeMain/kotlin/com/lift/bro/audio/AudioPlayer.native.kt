package com.lift.bro.audio

actual interface AudioPlayer {
    actual fun speak(text: String)
    actual fun stop()
}
