package io.github.tobyhs.trackannouncer

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class StopServiceReceiverTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val receiver = StopServiceReceiver()

    @Test
    fun onReceive() {
        receiver.onReceive(application, Intent())
        val stoppedIntent = shadowOf(application).nextStoppedService
        assertThat(shadowOf(stoppedIntent).intentClass, equalTo(TrackAnnouncerService::class.java))
    }
}
