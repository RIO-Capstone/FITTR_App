package com.example.fittr_app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object TextToSpeechHelper : TextToSpeech.OnInitListener {
    private var TAG = "TextToSpeechHelper"
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(context: Context):TextToSpeechHelper{
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
        return this
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
            isInitialized = true
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        } else {
            Log.e(TAG, "TTS not initialized")
        }
    }

    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
