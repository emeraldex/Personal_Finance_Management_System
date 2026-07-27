package com.expense.android.data

import android.content.Context
import java.math.BigDecimal

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

    /**
     * Fixed exchange rates: ISO code -> units of the app currency per one unit
     * of that currency, sorted by code. Unparseable hand-edited values are
     * dropped rather than surfaced.
     */
    fun fxRates(): Map<String, BigDecimal> =
        prefs.all.keys
            .filter { it.startsWith(KEY_FX_PREFIX) }
            .sorted()
            .mapNotNull { key ->
                prefs.getString(key, null)?.toBigDecimalOrNull()
                    ?.let { key.removePrefix(KEY_FX_PREFIX) to it }
            }
            .toMap()

    fun putFxRate(currencyCode: String, ratePerUnit: BigDecimal) {
        prefs.edit().putString(KEY_FX_PREFIX + currencyCode, ratePerUnit.toPlainString()).apply()
    }

    fun removeFxRate(currencyCode: String) {
        prefs.edit().remove(KEY_FX_PREFIX + currencyCode).apply()
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(trim()) } catch (e: NumberFormatException) { null }

    private companion object {
        const val KEY_BUDGET_ALERTS = "budgetAlerts"
        const val KEY_FX_PREFIX = "fxRate."
    }
}
