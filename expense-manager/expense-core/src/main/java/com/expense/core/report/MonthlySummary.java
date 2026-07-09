package com.expense.core.report;

import com.expense.core.util.Money;

import java.time.YearMonth;
import java.util.List;

/**
 * Rich, computed view of a single month, assembled by
 * {@link com.expense.core.service.MonthlySummaryService}. All figures are derived
 * from the underlying transactions and are safe to recompute at any time.
 *
 * <p>Sign conventions: {@code totalExpense} is non-positive, everything else
 * non-negative except {@code netBalance}, which may be either.</p>
 *
 * @param month             the month summarised
 * @param totalIncome       sum of income
 * @param totalExpense      sum of expenses (non-positive)
 * @param netBalance        totalIncome + totalExpense
 * @param savings           surplus kept this month (netBalance when positive, else 0)
 * @param outstanding       shortfall carried this month (|netBalance| when negative, else 0)
 * @param categoryBreakdown expense totals per category, descending by magnitude
 * @param paymentBreakdown  expense totals per payment method
 * @param cashFlow          per-day cash-flow series
 * @param budgetUtilization per-category budget-vs-actual
 */
public record MonthlySummary(YearMonth month, Money totalIncome, Money totalExpense,
                             Money netBalance, Money savings, Money outstanding,
                             List<CategoryBreakdownItem> categoryBreakdown,
                             List<PaymentMethodBreakdownItem> paymentBreakdown,
                             List<CashFlowPoint> cashFlow,
                             List<BudgetUtilization> budgetUtilization) {

    public MonthlySummary {
        categoryBreakdown = List.copyOf(categoryBreakdown);
        paymentBreakdown = List.copyOf(paymentBreakdown);
        cashFlow = List.copyOf(cashFlow);
        budgetUtilization = List.copyOf(budgetUtilization);
    }
}
