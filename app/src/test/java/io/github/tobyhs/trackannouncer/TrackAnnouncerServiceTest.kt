package io.github.tobyhs.trackannouncer

import android.app.NotificationManager
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.speech.tts.TextToSpeech

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowMediaSessionManager
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class TrackAnnouncerServiceTest {
    @Test
    fun `onCreate foregrounds itself`() {
        runService { service ->
            assertThat(shadowOf(service).isLastForegroundNotificationAttached, equalTo(true))

            val notificationManager = service.getSystemService(NotificationManager::class.java)
            val channel = notificationManager
                .getNotificationChannel(TrackAnnouncerService.NOTIFICATION_CHANNEL_ID)
            assertThat(channel.name.toString(), equalTo("Service Notifications"))
            assertThat(channel.importance, equalTo(NotificationManager.IMPORTANCE_DEFAULT))

            val notifications = notificationManager.activeNotifications
            assertThat(notifications.size, equalTo(1))
            val notification = notifications.first()
            assertThat(notification.id, equalTo(TrackAnnouncerService.NOTIFICATION_ID))
            val channelId = notification.notification.channelId
            assertThat(channelId, equalTo(TrackAnnouncerService.NOTIFICATION_CHANNEL_ID))
            assertThat(notification.isOngoing, equalTo(true))
        }
    }

    @Test
    fun `onCreate creates a TextToSpeech`() {
        runService { service ->
            val ttsShadow = shadowOf(service.textToSpeech)
            assertThat(ttsShadow.utteranceProgressListener is ResumePlaySpeechListener, equalTo(true))

            ShadowToast.reset()
            ttsShadow.onInitListener.onInit(TextToSpeech.ERROR)
            val lastToastText = ShadowToast.getTextOfLatestToast()
            assertThat(lastToastText, equalTo("Track Announcer: Failed to initialize TextToSpeech"))
            assertThat(shadowOf(service).isStoppedBySelf, equalTo(true))
        }
    }

    @Test
    fun `onCreate adds a SessionsChangedListener to the MediaSessionManager`() {
        runService { service ->
            val msmShadow = shadowOf(service.getSystemService(MediaSessionManager::class.java))
            val controller: MediaController = mockk()
            justRun { controller.registerCallback(any()) }
            justRun { controller.unregisterCallback(any()) }
            msmShadow.addController(controller)
            verify { controller.registerCallback(ofType(MetadataChangedCallback::class)) }
        }
    }

    @Test
    fun `onBind returns null`() {
        assertThat(TrackAnnouncerService().onBind(Intent()), nullValue())
    }

    @Test
    fun onDestroy() {
        val controller: MediaController = mockk()
        justRun { controller.registerCallback(any()) }
        justRun { controller.unregisterCallback(any()) }
        lateinit var msmShadow: ShadowMediaSessionManager

        val service = runService { service ->
            msmShadow = shadowOf(service.getSystemService(MediaSessionManager::class.java))
            msmShadow.addController(controller)
        }

        val anotherController: MediaController = mockk()
        msmShadow.addController(anotherController)
        verify(exactly = 0) { anotherController.registerCallback(any()) }
        verify { controller.unregisterCallback(ofType(MetadataChangedCallback::class)) }
        assertThat(shadowOf(service.textToSpeech).isShutdown, equalTo(true))
    }

    private fun runService(action: (TrackAnnouncerService) -> Unit): TrackAnnouncerService {
        val controller = Robolectric.buildService(TrackAnnouncerService::class.java)
        val service = controller.create().get()
        try {
            action(service)
        } finally {
            controller.destroy()
        }
        return service
    }
}
