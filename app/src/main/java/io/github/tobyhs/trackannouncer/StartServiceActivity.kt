package io.github.tobyhs.trackannouncer

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that starts [TrackAnnouncerService]
 */
class StartServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, TrackAnnouncerService::class.java))
        finishAndRemoveTask()
    }
}
