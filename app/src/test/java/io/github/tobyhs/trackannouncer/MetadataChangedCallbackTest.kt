package io.github.tobyhs.trackannouncer

import android.media.AudioManager
import android.media.MediaMetadata
import android.speech.tts.TextToSpeech
import android.view.KeyEvent

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MetadataChangedCallbackTest {
    private val textToSpeech: TextToSpeech = mockk()
    private val audioManager: AudioManager = mockk()
    private val callback = MetadataChangedCallback(textToSpeech, audioManager)

    @Before
    fun setup() {
        every { audioManager.isMusicActive } returns true
    }

    @Test
    fun `onMetadataChanged with null MediaMetadata`() {
        callback.onMetadataChanged(null)
        checkNoActionsTaken()
    }

    @Test
    fun `onMetadataChanged with no title`() {
        callback.onMetadataChanged(MediaMetadata.Builder().build())
        checkNoActionsTaken()
    }

    @Test
    fun `onMetadataChanged with no music active`() {
        every { audioManager.isMusicActive } returns false
        val title = "Some Title"
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title).build()
        callback.onMetadataChanged(metadata)
        checkNoActionsTaken()
    }

    @Test
    fun `onMetadataChanged with same title twice`() {
        val title = "Some Title"
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title).build()
        val keyEvents: MutableList<KeyEvent> = mutableListOf()
        justRun { audioManager.dispatchMediaKeyEvent(capture(keyEvents)) }
        every { textToSpeech.speak(any(), any(), any(), any()) } returns TextToSpeech.SUCCESS
        repeat(2) { callback.onMetadataChanged(metadata) }

        assertThat(keyEvents.size, equalTo(2))
        assertThat(keyEvents[0].action, equalTo(KeyEvent.ACTION_DOWN))
        assertThat(keyEvents[0].keyCode, equalTo(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertThat(keyEvents[1].action, equalTo(KeyEvent.ACTION_UP))
        assertThat(keyEvents[1].keyCode, equalTo(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        verify(exactly = 1) { textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, title) }
    }

    @Test
    fun `onMetadataChanged with 2 different titles`() {
        val titles = listOf("First Title", "Second Title")
        val metadataList = titles.map {
            MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, it).build()
        }
        val keyEvents: MutableList<KeyEvent> = mutableListOf()
        justRun { audioManager.dispatchMediaKeyEvent(capture(keyEvents)) }
        every { textToSpeech.speak(any(), any(), any(), any()) } returns TextToSpeech.SUCCESS
        for (metadata in metadataList) { callback.onMetadataChanged(metadata) }

        assertThat(keyEvents.size, equalTo(4))
        for (keyEvent in keyEvents) {
            assertThat(keyEvent.keyCode, equalTo(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        }
        for (i in listOf(0, 2)) { assertThat(keyEvents[i].action, equalTo(KeyEvent.ACTION_DOWN)) }
        for (i in listOf(1, 3)) { assertThat(keyEvents[i].action, equalTo(KeyEvent.ACTION_UP)) }

        for (title in titles) {
            verify(exactly = 1) { textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, title) }
        }
    }

    private fun checkNoActionsTaken() {
        verify(exactly = 0) { audioManager.dispatchMediaKeyEvent(any()) }
        verify(exactly = 0) { textToSpeech.speak(any(), any(), any(), any()) }
    }
}
