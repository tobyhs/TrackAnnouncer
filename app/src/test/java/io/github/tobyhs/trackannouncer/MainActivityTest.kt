package io.github.tobyhs.trackannouncer

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.getIntents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyIterable

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val intentsRule = IntentsRule()

    private val app = getApplicationContext<Context>()
    private val notificationManagerShadow = shadowOf(
        app.getSystemService(NotificationManager::class.java)
    )
    private val serviceComponent = ComponentName(app, TrackAnnouncerService::class.java)

    @Test
    fun `notification listener access not granted`() {
        launch(MainActivity::class.java).use {
            intended(hasAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    @Test
    fun `notification listener access is granted`() {
        val component = ComponentName(app, NotificationListener::class.java)
        notificationManagerShadow.setNotificationListenerAccessGranted(component, true)
        launch(MainActivity::class.java).use {
            assertThat(getIntents(), emptyIterable())
        }
    }

    @Test
    fun `notifications not enabled`() {
        notificationManagerShadow.setNotificationsEnabled(false)
        launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val permissions = shadowOf(activity).lastRequestedPermission.requestedPermissions
                assertThat(permissions, equalTo(arrayOf(Manifest.permission.POST_NOTIFICATIONS)))
            }
        }
    }

    @Test
    fun `notifications are enabled`() {
        notificationManagerShadow.setNotificationsEnabled(true)
        launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertThat(shadowOf(activity).lastRequestedPermission, nullValue())
            }
        }
    }

    @Test
    fun `Start Service button`() {
        launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.start_service_button)).perform(click())
            scenario.onActivity { activity ->
                val actualComponent = shadowOf(activity).nextStartedService.component
                assertThat(actualComponent, equalTo(serviceComponent))
            }
        }
    }

    @Test
    fun `Stop Service button`() {
        launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.stop_service_button)).perform(click())
            scenario.onActivity { activity ->
                val actualComponent = shadowOf(activity).nextStoppedService.component
                assertThat(actualComponent, equalTo(serviceComponent))
            }
        }
    }
}
