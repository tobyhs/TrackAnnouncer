package io.github.tobyhs.trackannouncer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver that stops [TrackAnnouncerService]
 */
class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, TrackAnnouncerService::class.java))
    }
}
