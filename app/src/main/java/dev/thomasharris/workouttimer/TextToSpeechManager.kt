package dev.thomasharris.workouttimer

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class TextToSpeechManager(private val context: Context) : LifecycleObserver {
    private var tts: TextToSpeech? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun prepare() {
        tts = TextToSpeech(context) {
            if (it != TextToSpeech.SUCCESS) {
                Log.i(this::class.qualifiedName, "Failed to init tts")
                tts = null
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun shutdown() {
        tts?.shutdown()
        tts = null
    }

    fun speak(
        text: String,
        mode: Int = TextToSpeech.QUEUE_FLUSH,
        params: Bundle? = null,
        utteranceId: String = System.nanoTime().toString(),
    ) {
        tts?.speak(text, mode, params, utteranceId)
    }

}