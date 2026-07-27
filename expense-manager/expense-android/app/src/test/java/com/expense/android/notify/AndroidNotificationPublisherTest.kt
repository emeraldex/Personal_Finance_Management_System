package com.expense.android.notify

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.expense.core.network.AppNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Verifies the Android side of the NotificationPublisher seam on the JVM via
 * Robolectric: the channel is registered and alerts actually reach the
 * notification manager with the seam's title/body intact.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class AndroidNotificationPublisherTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun alert(title: String = "Budget exceeded") = AppNotification(
        AppNotification.Severity.ALERT, title, "Groceries spending is over its budget this month."
    )

    @Test
    fun postsAlertOnTheBudgetChannel() {
        AndroidNotificationPublisher(context).publish(alert())

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadow = Shadows.shadowOf(manager)
        assertEquals(1, shadow.allNotifications.size)
        val posted = shadow.allNotifications[0]
        assertEquals("Budget exceeded", posted.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(
            "Groceries spending is over its budget this month.",
            posted.extras.getString(Notification.EXTRA_TEXT),
        )
        assertEquals(AndroidNotificationPublisher.CHANNEL_ID, posted.channelId)
    }

    @Test
    fun registersTheChannelOnce() {
        AndroidNotificationPublisher(context)
        AndroidNotificationPublisher(context)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertEquals(1, Shadows.shadowOf(manager).notificationChannels.size)
    }

    @Test
    fun replacesPreviousAlertInsteadOfStacking() {
        val publisher = AndroidNotificationPublisher(context)
        publisher.publish(alert(title = "First"))
        publisher.publish(alert(title = "Second"))

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadow = Shadows.shadowOf(manager)
        assertEquals(1, shadow.allNotifications.size)
        assertEquals("Second", shadow.allNotifications[0].extras.getString(Notification.EXTRA_TITLE))
    }
}
