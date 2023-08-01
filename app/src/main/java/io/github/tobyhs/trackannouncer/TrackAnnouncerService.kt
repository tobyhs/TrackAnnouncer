package io.github.tobyhs.trackannouncer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.IBinder

/**
 * Service that speaks the track title when a track starts playing
 */
class TrackAnnouncerService : Service() {
    companion object {
        internal const val NOTIFICATION_ID = 1
        internal const val NOTIFICATION_CHANNEL_ID = "service"
    }

    override fun onCreate() {
        super.onCreate()
        foregroundSelf()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun foregroundSelf() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.service_notifications),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }
}
