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

    private lateinit var audioManager: AudioManager
    @VisibleForTesting internal lateinit var textToSpeech: TextToSpeech
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var sessionsChangedListener: SessionsChangedListener

    override fun onCreate() {
        super.onCreate()
        foregroundSelf()
        audioManager = getSystemService(AudioManager::class.java)
        initTextToSpeech()
        initMediaSessionManager()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        sessionsChangedListener.unregisterCallbacks()
        textToSpeech.shutdown()
        super.onDestroy()
    }

    private fun foregroundSelf() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.service_notifications),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(contentIntent)
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
    }
}
