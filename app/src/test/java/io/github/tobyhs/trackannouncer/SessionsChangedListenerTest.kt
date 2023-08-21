package io.github.tobyhs.trackannouncer

import android.media.session.MediaController

import io.mockk.clearMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

import org.junit.Test

class SessionsChangedListenerTest {
    private val metadataChangedCallback: MetadataChangedCallback = mockk()
    private val listener = SessionsChangedListener(metadataChangedCallback)

    @Test
    fun `onActiveSessionsChanged with null controllers`() {
        val controller: MediaController = mockk()
        justRun { controller.registerCallback(any()) }
        listener.onActiveSessionsChanged(listOf(controller))
        clearMocks(controller)

        listener.onActiveSessionsChanged(null)
        verify(exactly = 0) { controller.unregisterCallback(any()) }
    }

    @Test
    fun onActiveSessionsChanged() {
        val firstControllers: List<MediaController> = List(2) { mockk() }
        val secondControllers: List<MediaController> = List(2) { mockk() }
        val allControllers = firstControllers + secondControllers
        for (c in allControllers) { justRun { c.registerCallback(any()) } }

        listener.onActiveSessionsChanged(firstControllers)
        for (c in firstControllers) {
            verify { c.registerCallback(metadataChangedCallback) }
            clearMocks(c)
        }

        for (c in firstControllers) { justRun { c.unregisterCallback(any()) } }
        listener.onActiveSessionsChanged(secondControllers)
        for (c in firstControllers) { verify { c.unregisterCallback(metadataChangedCallback) } }
        for (c in secondControllers) { verify { c.registerCallback(metadataChangedCallback) } }
    }

    @Test
    fun unregisterCallbacks() {
        val controllers: List<MediaController> = List(2) { mockk() }
        for (c in controllers) {
            justRun { c.registerCallback(any()) }
            justRun { c.unregisterCallback(any()) }
        }

        listener.onActiveSessionsChanged(controllers)
        listener.unregisterCallbacks()
        for (c in controllers) { verify { c.unregisterCallback(metadataChangedCallback) } }
    }
}
