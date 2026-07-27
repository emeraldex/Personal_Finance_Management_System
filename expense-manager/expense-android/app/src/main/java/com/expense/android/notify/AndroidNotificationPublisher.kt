package com.expense.android.notify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.expense.android.R
import com.expense.core.network.AppNotification
import com.expense.core.network.NotificationPublisher

/**
 * [NotificationPublisher] backed by Android's notification system — the mobile
 * counterpart of the desktop tray publisher. Alerts land in the system shade on
 * a dedicated "Budget alerts" channel, so the user controls sound/importance in
 * system settings like any other app notification.
 *
 * Honours the seam's fire-and-forget contract: missing POST_NOTIFICATIONS
 * permission or any platform failure is swallowed, never disturbing the save
 * that triggered the alert.
 */
class AndroidNotificationPublisher(private val context: Context) : NotificationPublisher {

    init {
        val channel = NotificationChannel(
            CHANNEL_ID, "Budget alerts", NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Warnings when a category approaches or exceeds its monthly budget"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Permission is effectively checked via areNotificationsEnabled(); a race is
    // caught by the RuntimeException handler (SecurityException on API 33+).
    @SuppressLint("MissingPermission")
    override fun publish(notification: AppNotification) {
        try {
            val manager = NotificationManagerCompat.from(context)
            if (!manager.areNotificationsEnabled()) {
                return
            }
            val built = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_budget)
                .setContentTitle(notification.title())
                .setContentText(notification.body())
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body()))
                .setPriority(priorityFor(notification.severity()))
                .setAutoCancel(true)
                .build()
            manager.notify(NOTIFICATION_ID, built)
        } catch (ignored: RuntimeException) {
            // Fire-and-forget by contract.
        }
    }

    private fun priorityFor(severity: AppNotification.Severity): Int = when (severity) {
        AppNotification.Severity.INFO -> NotificationCompat.PRIORITY_LOW
        AppNotification.Severity.WARNING -> NotificationCompat.PRIORITY_DEFAULT
        AppNotification.Severity.ALERT -> NotificationCompat.PRIORITY_HIGH
    }

    companion object {
        const val CHANNEL_ID = "budget_alerts"

        /** Single id: a newer budget alert replaces the previous one instead of stacking. */
        const val NOTIFICATION_ID = 1001
    }
}
