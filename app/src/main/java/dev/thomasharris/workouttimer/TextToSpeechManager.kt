package dev.thomasharris.workouttimer

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TextToSpeechManager(private val context: Context) : LifecycleObserver {
    private var tts: TextToSpeech? = null

    private val _channel = MutableStateFlow(false)
    val isReady = _channel.asStateFlow()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun prepare() {
        tts = TextToSpeech(context) {
            when (it) {
                TextToSpeech.SUCCESS -> _channel.value = true
                else -> {
                    Log.i(this::class.qualifiedName, "Failed to init tts")
                    tts = null
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun shutdown() {
        _channel.value = false
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