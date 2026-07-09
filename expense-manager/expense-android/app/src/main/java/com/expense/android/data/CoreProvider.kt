package com.expense.android.data

import android.content.Context
import com.expense.core.service.ExpenseManager
import java.io.File
import java.util.Currency

/**
 * Process-wide bridge to the shared pure-Java core. Android UI code depends only
 * on [ExpenseManager] from `expense-core`; all business logic, validation and
 * analytics are reused unchanged from the desktop build.
 *
 * The core persists via JDBC/SQLite. On Android we point it at a file in the
 * app's private storage, so the exact same services and schema run on mobile.
 */
object CoreProvider {

    @Volatile
    private var instance: ExpenseManager? = null

    fun get(context: Context): ExpenseManager {
        return instance ?: synchronized(this) {
            instance ?: create(context).also { instance = it }
        }
    }

    private fun create(context: Context): ExpenseManager {
        val dbFile = File(context.filesDir, "expenses.db")
        return ExpenseManager.openFile(dbFile.absolutePath, Currency.getInstance("USD"))
    }

    fun shutdown() {
        synchronized(this) {
            instance?.close()
            instance = null
        }
    }
}
