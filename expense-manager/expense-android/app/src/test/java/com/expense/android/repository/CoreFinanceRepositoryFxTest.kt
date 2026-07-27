package com.expense.android.repository

import android.content.Context
import com.expense.android.data.AndroidAccountRepository
import com.expense.android.data.AndroidBudgetRepository
import com.expense.android.data.AndroidCategoryRepository
import com.expense.android.data.AndroidDatabase
import com.expense.android.data.AndroidExpenseRepository
import com.expense.android.data.AndroidIncomeRepository
import com.expense.android.data.AndroidMonthlySummaryRepository
import com.expense.android.data.AndroidPaymentMethodRepository
import com.expense.core.domain.AccountType
import com.expense.core.domain.CategoryType
import com.expense.core.dto.CreateAccountRequest
import com.expense.core.dto.CreateCategoryRequest
import com.expense.core.service.ExpenseManager
import com.expense.core.util.Money
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Currency

/**
 * Exercises the exchange-rate seam through the Android repository on the JVM
 * via Robolectric: foreign-currency entry converts to the app currency before
 * persisting, and a missing rate saves nothing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CoreFinanceRepositoryFxTest {

    private val myr: Currency = Currency.getInstance("MYR")
    private val usd: Currency = Currency.getInstance("USD")

    private lateinit var db: AndroidDatabase
    private lateinit var manager: ExpenseManager
    private lateinit var repository: CoreFinanceRepository
    private var accountId: Long = 0

    @Before
    fun setUp() {
        val ctx: Context = RuntimeEnvironment.getApplication()
        val file = ctx.getDatabasePath("fx-test-" + System.nanoTime() + ".db")
        file.parentFile?.mkdirs()
        db = AndroidDatabase.open(file.absolutePath)
        manager = ExpenseManager(
            AndroidCategoryRepository(db),
            AndroidPaymentMethodRepository(db),
            AndroidAccountRepository(db),
            AndroidExpenseRepository(db),
            AndroidIncomeRepository(db),
            AndroidBudgetRepository(db),
            AndroidMonthlySummaryRepository(db),
            myr,
            db,
        )
        repository = CoreFinanceRepository(manager, myr)
        accountId = manager.accounts()
            .create(CreateAccountRequest("Cash", AccountType.CASH, Money.zero(myr))).id()
        manager.categories()
            .create(CreateCategoryRequest("Groceries", CategoryType.EXPENSE, "#4CAF50", "cart"))
    }

    @After
    fun tearDown() {
        manager.close()
    }

    @Test
    fun convertsForeignEntryToAppCurrencyBeforePersisting() {
        manager.exchangeRates().setRate(usd, myr, BigDecimal("4.70"))
        val note = repository.addExpense(accountId, null, BigDecimal("25.00"), "abroad", "USD")
        assertEquals("Saved (USD 25.00 → MYR 117.50)", note)

        val stored = manager.expenses().listByMonth(YearMonth.now()).single()
        assertEquals(Money.of("-117.50", myr), stored.signedAmount())
    }

    @Test
    fun missingRateSavesNothing() {
        val note = repository.addExpense(accountId, null, BigDecimal("25.00"), "abroad", "USD")
        assertNull(note)
        assertTrue(manager.expenses().listByMonth(YearMonth.now()).isEmpty())
    }

    @Test
    fun appCurrencyEntryIsUnconverted() {
        val note = repository.addExpense(accountId, null, BigDecimal("12.00"), "home", null)
        assertEquals("Saved", note)
        assertEquals(Money.of("-12.00", myr),
            manager.expenses().listByMonth(YearMonth.now()).single().signedAmount())
    }

    @Test
    fun entryCurrencyCodesListAppCurrencyFirstThenConfigured() {
        manager.exchangeRates().setRate(usd, myr, BigDecimal("4.70"))
        assertEquals(listOf("MYR", "USD"), repository.entryCurrencyCodes())
    }
}
