package com.expense.core.service;

import com.expense.core.domain.Budget;
import com.expense.core.domain.Category;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.MonthlySummarySnapshot;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.repository.BudgetRepository;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.repository.ExpenseRepository;
import com.expense.core.repository.IncomeRepository;
import com.expense.core.repository.MonthlySummaryRepository;
import com.expense.core.repository.PaymentMethodRepository;
import com.expense.core.report.BudgetUtilization;
import com.expense.core.report.CashFlowPoint;
import com.expense.core.report.CategoryBreakdownItem;
import com.expense.core.report.MonthlySummary;
import com.expense.core.report.PaymentMethodBreakdownItem;
import com.expense.core.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Computes the rich {@link MonthlySummary} for a month and, when requested,
 * persists a {@link MonthlySummarySnapshot} cache row.
 *
 * <h2>Definitions (iteration&nbsp;1)</h2>
 * <ul>
 *   <li><b>Total income</b> — sum of income amounts (>= 0).</li>
 *   <li><b>Total expenses</b> — sum of expense amounts (<= 0).</li>
 *   <li><b>Net balance</b> — total income + total expenses.</li>
 *   <li><b>Savings</b> — the surplus kept this month: {@code max(net, 0)}.</li>
 *   <li><b>Outstanding</b> — the shortfall covered by debt this month:
 *       {@code max(-net, 0)}.</li>
 *   <li><b>Category / payment breakdown</b> — expense totals grouped and ranked
 *       by magnitude, with each share as a percentage of total spend.</li>
 *   <li><b>Cash flow</b> — per-day income/expense/net series.</li>
 *   <li><b>Budget utilisation</b> — spend versus each category's monthly cap.</li>
 * </ul>
 * Savings and outstanding are mutually exclusive and derivable purely from the
 * month's transactions; later iterations can refine them with explicit savings
 * transfers and a paid/unpaid flag without changing this API.
 */
public final class MonthlySummaryService {

    private final ExpenseRepository expenses;
    private final IncomeRepository incomes;
    private final CategoryRepository categories;
    private final PaymentMethodRepository paymentMethods;
    private final BudgetRepository budgets;
    private final MonthlySummaryRepository summaries;
    private final Currency defaultCurrency;

    public MonthlySummaryService(ExpenseRepository expenses, IncomeRepository incomes,
                                 CategoryRepository categories, PaymentMethodRepository paymentMethods,
                                 BudgetRepository budgets, MonthlySummaryRepository summaries,
                                 Currency defaultCurrency) {
        this.expenses = Objects.requireNonNull(expenses);
        this.incomes = Objects.requireNonNull(incomes);
        this.categories = Objects.requireNonNull(categories);
        this.paymentMethods = Objects.requireNonNull(paymentMethods);
        this.budgets = Objects.requireNonNull(budgets);
        this.summaries = Objects.requireNonNull(summaries);
        this.defaultCurrency = Objects.requireNonNull(defaultCurrency);
    }

    /** Computes the full summary for {@code month}. Pure: performs no writes. */
    public MonthlySummary summarize(YearMonth month) {
        List<Expense> monthExpenses = expenses.findByMonth(month);
        List<Income> monthIncomes = incomes.findByMonth(month);
        Currency ccy = resolveCurrency(monthExpenses, monthIncomes);

        Money totalIncome = sumIncome(monthIncomes, ccy);
        Money totalExpense = sumExpense(monthExpenses, ccy);
        Money net = totalIncome.add(totalExpense);
        Money savings = net.isPositive() ? net : Money.zero(ccy);
        Money outstanding = net.isNegative() ? net.negate() : Money.zero(ccy);

        return new MonthlySummary(month, totalIncome, totalExpense, net, savings, outstanding,
                categoryBreakdown(monthExpenses, ccy),
                paymentBreakdown(monthExpenses, ccy),
                cashFlow(month, monthExpenses, monthIncomes, ccy),
                budgetUtilization(month, monthExpenses, ccy));
    }

    /** Computes the summary and upserts a cached snapshot row for fast dashboard loads. */
    public MonthlySummarySnapshot summarizeAndCache(YearMonth month) {
        MonthlySummary s = summarize(month);
        MonthlySummarySnapshot snapshot = new MonthlySummarySnapshot(
                null, month, s.totalIncome(), s.totalExpense(), s.netBalance(),
                s.savings(), s.outstanding(), Instant.now());
        return summaries.upsert(snapshot);
    }

    // --- helpers -----------------------------------------------------------

    private Money sumIncome(List<Income> list, Currency ccy) {
        Money total = Money.zero(ccy);
        for (Income i : list) {
            total = total.add(i.amount());
        }
        return total;
    }

    private Money sumExpense(List<Expense> list, Currency ccy) {
        Money total = Money.zero(ccy);
        for (Expense e : list) {
            total = total.add(e.amount());
        }
        return total;
    }

    private List<CategoryBreakdownItem> categoryBreakdown(List<Expense> monthExpenses, Currency ccy) {
        Map<Long, Money> totals = new HashMap<>();
        for (Expense e : monthExpenses) {
            totals.merge(keyOf(e.categoryId()), e.amount(), Money::add);
        }
        Money grandTotal = sumExpense(monthExpenses, ccy).abs();
        List<CategoryBreakdownItem> items = new ArrayList<>();
        for (Map.Entry<Long, Money> entry : totals.entrySet()) {
            Long categoryId = entry.getKey() == 0L ? null : entry.getKey();
            String name = categoryName(categoryId);
            double pct = percentage(entry.getValue().abs(), grandTotal);
            items.add(new CategoryBreakdownItem(categoryId, name, entry.getValue(), pct));
        }
        items.sort(Comparator.comparing((CategoryBreakdownItem i) -> i.total().abs()).reversed());
        return items;
    }

    private List<PaymentMethodBreakdownItem> paymentBreakdown(List<Expense> monthExpenses, Currency ccy) {
        Map<Long, Money> totals = new HashMap<>();
        for (Expense e : monthExpenses) {
            totals.merge(keyOf(e.paymentMethodId()), e.amount(), Money::add);
        }
        Money grandTotal = sumExpense(monthExpenses, ccy).abs();
        List<PaymentMethodBreakdownItem> items = new ArrayList<>();
        for (Map.Entry<Long, Money> entry : totals.entrySet()) {
            Long pmId = entry.getKey() == 0L ? null : entry.getKey();
            String name = paymentMethodName(pmId);
            double pct = percentage(entry.getValue().abs(), grandTotal);
            items.add(new PaymentMethodBreakdownItem(pmId, name, entry.getValue(), pct));
        }
        items.sort(Comparator.comparing((PaymentMethodBreakdownItem i) -> i.total().abs()).reversed());
        return items;
    }

    private List<CashFlowPoint> cashFlow(YearMonth month, List<Expense> monthExpenses,
                                         List<Income> monthIncomes, Currency ccy) {
        Map<LocalDate, Money> incomeByDay = new LinkedHashMap<>();
        Map<LocalDate, Money> expenseByDay = new LinkedHashMap<>();
        for (Income i : monthIncomes) {
            incomeByDay.merge(i.date(), i.amount(), Money::add);
        }
        for (Expense e : monthExpenses) {
            expenseByDay.merge(e.date(), e.amount(), Money::add);
        }
        List<CashFlowPoint> points = new ArrayList<>();
        LocalDate day = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        while (!day.isAfter(end)) {
            Money in = incomeByDay.getOrDefault(day, Money.zero(ccy));
            Money out = expenseByDay.getOrDefault(day, Money.zero(ccy));
            if (!in.isZero() || !out.isZero()) {
                points.add(new CashFlowPoint(day, in, out, in.add(out)));
            }
            day = day.plusDays(1);
        }
        return points;
    }

    private List<BudgetUtilization> budgetUtilization(YearMonth month, List<Expense> monthExpenses,
                                                      Currency ccy) {
        Map<Long, Money> spentByCategory = new HashMap<>();
        for (Expense e : monthExpenses) {
            if (e.categoryId() != null) {
                spentByCategory.merge(e.categoryId(), e.amount().abs(), Money::add);
            }
        }
        List<BudgetUtilization> result = new ArrayList<>();
        for (Budget b : budgets.findByMonth(month)) {
            Money spent = spentByCategory.getOrDefault(b.categoryId(), Money.zero(ccy));
            Money remaining = b.limit().subtract(spent);
            double pct = b.limit().isZero() ? 0.0
                    : percentage(spent, b.limit());
            boolean over = spent.compareTo(b.limit()) > 0;
            result.add(new BudgetUtilization(b.categoryId(), categoryName(b.categoryId()),
                    b.limit(), spent, remaining, pct, over));
        }
        result.sort(Comparator.comparingDouble(BudgetUtilization::utilizationPct).reversed());
        return result;
    }

    private Currency resolveCurrency(List<Expense> e, List<Income> i) {
        if (!e.isEmpty()) return e.get(0).amount().currency();
        if (!i.isEmpty()) return i.get(0).amount().currency();
        return defaultCurrency;
    }

    private static long keyOf(Long id) {
        return id == null ? 0L : id;
    }

    private String categoryName(Long id) {
        if (id == null) return "Uncategorised";
        return categories.findById(id).map(Category::name).orElse("Unknown");
    }

    private String paymentMethodName(Long id) {
        if (id == null) return "None";
        return paymentMethods.findById(id).map(PaymentMethod::name).orElse("Unknown");
    }

    private static double percentage(Money part, Money whole) {
        if (whole.isZero()) return 0.0;
        return part.amount()
                .divide(whole.amount(), 6, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_EVEN)
                .doubleValue();
    }
}
