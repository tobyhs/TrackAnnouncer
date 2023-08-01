package io.github.tobyhs.trackannouncer

import android.app.NotificationManager
import android.content.Intent

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class TrackAnnouncerServiceTest {
    @Test
    fun `onCreate foregrounds itself`() {
        val serviceController = Robolectric.buildService(TrackAnnouncerService::class.java)
        try {
            val service = serviceController.create().get()
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
        } finally {
            serviceController.destroy()
        }
    }

    @Test
    fun `onBind returns null`() {
        assertThat(TrackAnnouncerService().onBind(Intent()), nullValue())
    }
}
