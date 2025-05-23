package com.example.vocalise.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// manage all tts functionality here
// singleton, so only one instance of this is used in the whole app
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    private var tts: TextToSpeech? = null

    // initialisation block that runs when class is first created
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.UK
            }
        }
    }

    // makes tts speak a given string
    fun speak(text: String) {
        // speaks immediately, cancels anything alr bein spoken
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null , null)
    }

    // cleanup method
    fun shutdown() {
        tts?.shutdown()
    }
}