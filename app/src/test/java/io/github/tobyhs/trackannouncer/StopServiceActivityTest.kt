package io.github.tobyhs.trackannouncer

import android.app.Application
import android.content.ComponentName

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class StopServiceActivityTest {
    @Test
    fun onCreate() {
        val app = getApplicationContext<Application>()
        val scenario = launch(StopServiceActivity::class.java)
        scenario.use {
            val actualComponent = shadowOf(app).nextStoppedService.component
            assertThat(
                actualComponent,
                equalTo(ComponentName(app, TrackAnnouncerService::class.java))
            )
            assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
        }
    }
}
