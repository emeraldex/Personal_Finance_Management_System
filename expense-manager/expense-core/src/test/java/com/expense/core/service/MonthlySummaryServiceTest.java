package com.expense.core.service;

import com.expense.core.domain.MonthlySummarySnapshot;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.report.BudgetUtilization;
import com.expense.core.report.CategoryBreakdownItem;
import com.expense.core.report.MonthlySummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class MonthlySummaryServiceTest extends CoreTestBase {

    private final YearMonth JAN = YearMonth.of(2026, 1);

    private void seedJanuary() {
        manager.incomes().create(new CreateIncomeRequest(
                account.id(), salary.id(), usd("3000.00"), "Salary", LocalDate.of(2026, 1, 1)));
        manager.expenses().create(new CreateExpenseRequest(
                account.id(), rent.id(), cash.id(), usd("1000.00"), "Rent", LocalDate.of(2026, 1, 3)));
        manager.expenses().create(new CreateExpenseRequest(
                account.id(), groceries.id(), cash.id(), usd("500.00"), "Shop 1", LocalDate.of(2026, 1, 5)));
        manager.expenses().create(new CreateExpenseRequest(
                account.id(), groceries.id(), cash.id(), usd("200.00"), "Shop 2", LocalDate.of(2026, 1, 10)));
    }

    @Test
    void computesHeadlineFiguresWithCorrectSigns() {
        seedJanuary();
        MonthlySummary s = manager.summaries().summarize(JAN);

        assertEquals(usd("3000.00"), s.totalIncome());
        assertEquals(usd("-1700.00"), s.totalExpense());
        assertEquals(usd("1300.00"), s.netBalance());
        assertEquals(usd("1300.00"), s.savings());
        assertEquals(usd("0.00"), s.outstanding());
    }

    @Test
    void outstandingIsShortfallWhenOverspent() {
        // Only spend, no income -> net negative -> outstanding = |net|, savings = 0
        manager.expenses().create(new CreateExpenseRequest(
                account.id(), rent.id(), cash.id(), usd("800.00"), "Rent", LocalDate.of(2026, 1, 3)));
        MonthlySummary s = manager.summaries().summarize(JAN);
        assertEquals(usd("-800.00"), s.netBalance());
        assertEquals(usd("0.00"), s.savings());
        assertEquals(usd("800.00"), s.outstanding());
    }

    @Test
    void categoryBreakdownIsRankedByMagnitudeWithPercentages() {
        seedJanuary();
        MonthlySummary s = manager.summaries().summarize(JAN);

        assertEquals(2, s.categoryBreakdown().size());
        CategoryBreakdownItem top = s.categoryBreakdown().get(0);
        assertEquals("Rent", top.categoryName());
        assertEquals(usd("-1000.00"), top.total());
        // Rent share of 1700 total spend
        assertEquals(58.82, top.percentage(), 0.01);

        CategoryBreakdownItem second = s.categoryBreakdown().get(1);
        assertEquals("Groceries", second.categoryName());
        assertEquals(usd("-700.00"), second.total());
        assertEquals(41.18, second.percentage(), 0.01);
    }

    @Test
    void paymentBreakdownAggregatesBySingleMethod() {
        seedJanuary();
        MonthlySummary s = manager.summaries().summarize(JAN);
        assertEquals(1, s.paymentBreakdown().size());
        assertEquals("Cash", s.paymentBreakdown().get(0).name());
        assertEquals(usd("-1700.00"), s.paymentBreakdown().get(0).total());
        assertEquals(100.0, s.paymentBreakdown().get(0).percentage(), 0.01);
    }

    @Test
    void budgetUtilizationFlagsOverspend() {
        seedJanuary();
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("600.00")));
        MonthlySummary s = manager.summaries().summarize(JAN);

        assertEquals(1, s.budgetUtilization().size());
        BudgetUtilization u = s.budgetUtilization().get(0);
        assertEquals("Groceries", u.categoryName());
        assertEquals(usd("700.00"), u.spent());
        assertEquals(usd("-100.00"), u.remaining());
        assertTrue(u.overBudget());
        assertEquals(116.67, u.utilizationPct(), 0.01);
    }

    @Test
    void cashFlowHasOnePointPerActiveDay() {
        seedJanuary();
        MonthlySummary s = manager.summaries().summarize(JAN);
        // days with activity: Jan 1, 3, 5, 10
        assertEquals(4, s.cashFlow().size());
        assertEquals(usd("3000.00"), s.cashFlow().get(0).income());
        assertEquals(usd("3000.00"), s.cashFlow().get(0).net());
    }

    @Test
    void summarizeAndCachePersistsSnapshot() {
        seedJanuary();
        MonthlySummarySnapshot snap = manager.summaries().summarizeAndCache(JAN);
        assertNotNull(snap.id());
        assertEquals(usd("1300.00"), snap.netBalance());

        // re-caching upserts (same row, no duplicate)
        MonthlySummarySnapshot again = manager.summaries().summarizeAndCache(JAN);
        assertEquals(snap.id(), again.id());
    }

    @Test
    void emptyMonthYieldsZeroesInDefaultCurrency() {
        MonthlySummary s = manager.summaries().summarize(YearMonth.of(2030, 6));
        assertEquals(usd("0.00"), s.totalIncome());
        assertEquals(usd("0.00"), s.totalExpense());
        assertEquals(usd("0.00"), s.netBalance());
        assertTrue(s.categoryBreakdown().isEmpty());
    }
}
