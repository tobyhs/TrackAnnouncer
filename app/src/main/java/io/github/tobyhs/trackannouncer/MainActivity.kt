package io.github.tobyhs.trackannouncer

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import androidx.appcompat.app.AppCompatActivity

import io.github.tobyhs.trackannouncer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, TrackAnnouncerService::class.java)
        binding.startServiceButton.setOnClickListener { startService(serviceIntent) }
        binding.stopServiceButton.setOnClickListener { stopService(serviceIntent) }

        val notificationManager = getSystemService(NotificationManager::class.java)
        val listenerComponent = ComponentName(this, NotificationListener::class.java)
        if (!notificationManager.isNotificationListenerAccessGranted(listenerComponent)) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationManager.areNotificationsEnabled()
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }
}
