package io.github.tobyhs.trackannouncer

import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.view.KeyEvent

import io.mockk.justRun
import io.mockk.mockk

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResumePlaySpeechListenerTest {
    private val audioManager: AudioManager = mockk()
    private val listener = ResumePlaySpeechListener(audioManager)

    @Test
    fun onStart() {
        listener.onStart("Title")
    }

    @Test
    fun onDone() {
        val keyEvents: MutableList<KeyEvent> = mutableListOf()
        justRun { audioManager.dispatchMediaKeyEvent(capture(keyEvents)) }
        listener.onDone("Title")

        assertThat(keyEvents.size, equalTo(2))
        for (event in keyEvents) {
            assertThat(event.keyCode, equalTo(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        }
        assertThat(keyEvents[0].action, equalTo(KeyEvent.ACTION_DOWN))
        assertThat(keyEvents[1].action, equalTo(KeyEvent.ACTION_UP))
    }

    @Test
    fun onError() {
        listener.onError("Title", TextToSpeech.ERROR_SYNTHESIS)
    }
}
