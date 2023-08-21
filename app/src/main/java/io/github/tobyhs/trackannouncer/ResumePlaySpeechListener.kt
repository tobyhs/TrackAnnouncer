package io.github.tobyhs.trackannouncer

import android.media.AudioManager
import android.speech.tts.UtteranceProgressListener
import android.view.KeyEvent

/**
 * An [UtteranceProgressListener] that resumes media playback when speech (triggered by
 * [MetadataChangedCallback]) is done.
 */
class ResumePlaySpeechListener(
    private val audioManager: AudioManager,
) : UtteranceProgressListener() {
    override fun onStart(utteranceId: String?) {}

    override fun onDone(utteranceId: String?) {
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        )
    }

    @Deprecated("")
    override fun onError(utteranceId: String?) {}
}
