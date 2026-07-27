package com.expense.core.service;

import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.network.AppNotification;
import com.expense.core.network.BankFeedEntry;
import com.expense.core.report.ImportResult;
import com.expense.core.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankFeedImportServiceTest extends CoreTestBase {

    private static final Currency MYR = Currency.getInstance("MYR");
    private static final LocalDate DAY = LocalDate.of(2026, 1, 10);
    private static final YearMonth JAN = YearMonth.of(2026, 1);

    private BankFeedEntry entry(String description, String amount) {
        return new BankFeedEntry(DAY, description, usd(amount), "id-" + description + amount);
    }

    @Test
    void routesDebitsToExpensesAndCreditsToIncome() {
        ImportResult result = manager.bankFeedImports().importInto(account.id(), List.of(
                entry("GROCERIES MARKET", "-120.00"),
                entry("SALARY JAN", "3000.00")));
        assertEquals(2, result.imported());
        assertEquals(0, result.skipped());

        Expense expense = manager.expenses().listByMonth(JAN).get(0);
        assertEquals(usd("-120.00"), expense.signedAmount());
        // Composed categoriser seam: "GROCERIES MARKET" matches the Groceries category.
        assertEquals(groceries.id(), expense.categoryId());

        Income income = manager.incomes().listByMonth(JAN).get(0);
        assertEquals(usd("3000.00"), income.signedAmount());
    }

    @Test
    void reimportingTheSameEntriesIsIdempotent() {
        List<BankFeedEntry> entries = List.of(
                entry("GROCERIES MARKET", "-120.00"),
                entry("SALARY JAN", "3000.00"));
        manager.bankFeedImports().importInto(account.id(), entries);
        ImportResult second = manager.bankFeedImports().importInto(account.id(), entries);
        assertEquals(0, second.imported());
        assertEquals(2, second.skipped());
        assertEquals(1, manager.expenses().listByMonth(JAN).size());
        assertEquals(1, manager.incomes().listByMonth(JAN).size());
    }

    @Test
    void convertsForeignEntriesThroughTheExchangeRateSeam() {
        manager.exchangeRates().setRate(MYR, USD, new BigDecimal("0.25"));
        BankFeedEntry foreign = new BankFeedEntry(DAY, "KL TAXI", Money.of("-100.00", MYR), "fx-1");
        ImportResult result = manager.bankFeedImports().importInto(account.id(), List.of(foreign));
        assertEquals(1, result.imported());
        assertEquals(usd("-25.00"), manager.expenses().listByMonth(JAN).get(0).signedAmount());
    }

    @Test
    void skipsForeignEntriesWithoutARate() {
        BankFeedEntry foreign = new BankFeedEntry(DAY, "KL TAXI", Money.of("-100.00", MYR), "fx-1");
        ImportResult result = manager.bankFeedImports().importInto(account.id(), List.of(foreign));
        assertEquals(0, result.imported());
        assertEquals(1, result.skipped());
        assertTrue(result.warnings().get(0).contains("MYR"));
        assertTrue(manager.expenses().listByMonth(JAN).isEmpty());
    }

    @Test
    void checksAffectedBudgetsOncePerCategoryAfterImport() {
        List<AppNotification> published = new ArrayList<>();
        BudgetAlertService alerts = new BudgetAlertService(manager.summaries(), published::add);
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        manager.bankFeedImports().importInto(account.id(), List.of(
                entry("GROCERIES MARKET", "-80.00"),
                entry("GROCERIES MARKET AGAIN", "-40.00")), alerts);
        // Two imported debits, one affected budget -> exactly one notification.
        assertEquals(1, published.size());
        assertEquals(AppNotification.Severity.ALERT, published.get(0).severity());
    }

    @Test
    void publishesNoAlertWhenImportStaysUnderBudget() {
        List<AppNotification> published = new ArrayList<>();
        BudgetAlertService alerts = new BudgetAlertService(manager.summaries(), published::add);
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("500.00")));
        manager.bankFeedImports().importInto(account.id(),
                List.of(entry("GROCERIES MARKET", "-80.00")), alerts);
        assertTrue(published.isEmpty());
    }

    @Test
    void skipsZeroAmountsWithAWarning() {
        ImportResult result = manager.bankFeedImports().importInto(account.id(),
                List.of(entry("VOID", "0.00")));
        assertEquals(0, result.imported());
        assertEquals(1, result.skipped());
        assertEquals(1, result.warnings().size());
    }
}
