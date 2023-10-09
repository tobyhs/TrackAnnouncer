package io.github.tobyhs.trackannouncer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.os.IBinder
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast

import androidx.annotation.VisibleForTesting

/**
 * Service that speaks the track title when a track starts playing
 */
class TrackAnnouncerService : Service() {
    companion object {
        internal const val NOTIFICATION_ID = 1
        internal const val NOTIFICATION_CHANNEL_ID = "service"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    @VisibleForTesting internal lateinit var textToSpeech: TextToSpeech
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var sessionsChangedListener: SessionsChangedListener

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NotificationManager::class.java)
        val listenerComponent = ComponentName(this, NotificationListener::class.java)
        if (!notificationManager.isNotificationListenerAccessGranted(listenerComponent)) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            Toast.makeText(this, R.string.grant_notification_access, Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        foregroundSelf()
        audioManager = getSystemService(AudioManager::class.java)
        initTextToSpeech()
        initMediaSessionManager()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            sessionsChangedListener.unregisterCallbacks()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    private fun foregroundSelf() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.service_notifications),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopServiceIntent = PendingIntent.getActivity(
            this, 0, Intent(this, StopServiceActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopServiceAction = Notification.Action.Builder(
            null,
            getString(R.string.stop_service),
            stopServiceIntent
        ).build()
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(stopServiceAction)
            .build()
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(this, R.string.tts_init_failure, Toast.LENGTH_LONG).show()
                stopSelf()
            }
        }
        textToSpeech.setOnUtteranceProgressListener(ResumePlaySpeechListener(audioManager))
    }

    private fun initMediaSessionManager() {
        mediaSessionManager = getSystemService(MediaSessionManager::class.java)
        val listenerComponent = ComponentName(this, NotificationListener::class.java)
        sessionsChangedListener = SessionsChangedListener(
            MetadataChangedCallback(textToSpeech, audioManager)
        )
        mediaSessionManager.addOnActiveSessionsChangedListener(
            sessionsChangedListener,
            listenerComponent
        )
        sessionsChangedListener.onActiveSessionsChanged(
            mediaSessionManager.getActiveSessions(listenerComponent)
        )
    }
}
