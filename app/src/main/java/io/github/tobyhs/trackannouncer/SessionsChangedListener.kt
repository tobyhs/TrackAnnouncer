package io.github.tobyhs.trackannouncer

import android.media.session.MediaController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener

/**
 * A listener that registers a [MetadataChangedCallback] on active media sessions
 */
class SessionsChangedListener(
    private val metadataChangedCallback: MetadataChangedCallback
) : OnActiveSessionsChangedListener {
    private var controllers: List<MediaController> = emptyList()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        controllers ?: return
        unregisterCallbacks()
        this.controllers = controllers
        for (controller in controllers) {
            controller.registerCallback(metadataChangedCallback)
        }
    }

    /**
     * Unregisters the [MetadataChangedCallback] on the most recent active sessions
     */
    fun unregisterCallbacks() {
        for (controller in controllers) {
            controller.unregisterCallback(metadataChangedCallback)
        }
    }
}
