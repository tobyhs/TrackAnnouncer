package io.github.tobyhs.trackannouncer

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that stops [TrackAnnouncerService]
 */
class StopServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopService(Intent(this, TrackAnnouncerService::class.java))
        finishAndRemoveTask()
    }
}
