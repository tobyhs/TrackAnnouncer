package io.github.tobyhs.trackannouncer

import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.speech.tts.TextToSpeech
import android.view.KeyEvent

/**
 * A callback for speaking the track title when the track changes.
 */
class MetadataChangedCallback(
    private val textToSpeech: TextToSpeech,
    private val audioManager: AudioManager,
) : MediaController.Callback() {
    private var lastTitle: String? = null

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        metadata ?: return
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return
        if (title == lastTitle || !audioManager.isMusicActive) {
            return
        }
        lastTitle = title
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        )
        textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, title)
    }
}
