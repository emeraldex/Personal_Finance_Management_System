package com.expense.android.data

import android.content.Context

/**
 * File-backed user preferences — the Android counterpart of the desktop
 * `Settings` class. Reads consult SharedPreferences directly so a change
 * applies immediately wherever the preference is consulted at use time.
 */
class AppPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Whether budget alerts may be delivered as system notifications. */
    var budgetAlerts: Boolean
        get() = prefs.getBoolean(KEY_BUDGET_ALERTS, true)
        set(value) {
            prefs.edit().putBoolean(KEY_BUDGET_ALERTS, value).apply()
        }

    private companion object {
        const val KEY_BUDGET_ALERTS = "budgetAlerts"
    }
}
