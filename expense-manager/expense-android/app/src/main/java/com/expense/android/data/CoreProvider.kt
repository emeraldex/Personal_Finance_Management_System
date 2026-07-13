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
 * Persistence uses `android.database.sqlite` directly via the `Android*Repository`
 * adapters (implementing the core repository ports), so the app needs **no** JDBC
 * driver and runs on real devices. The same services, schema and analytics that
 * power the desktop therefore run on mobile — only the storage adapter differs.
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
        val database = AndroidDatabase.open(dbFile.absolutePath)
        return ExpenseManager(
            AndroidCategoryRepository(database),
            AndroidPaymentMethodRepository(database),
            AndroidAccountRepository(database),
            AndroidExpenseRepository(database),
            AndroidIncomeRepository(database),
            AndroidBudgetRepository(database),
            AndroidMonthlySummaryRepository(database),
            Currency.getInstance("MYR"),
            database,
        )
    }

    fun shutdown() {
        synchronized(this) {
            instance?.close()
            instance = null
        }
    }
}
