package com.expense.core.service;

import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.network.BankFeedClient;
import com.expense.core.network.BankFeedEntry;
import com.expense.core.network.CategorySuggestion;
import com.expense.core.network.ExpenseCategorizer;
import com.expense.core.report.ImportResult;
import com.expense.core.util.Money;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Imports {@link BankFeedEntry} drafts produced by the {@link BankFeedClient}
 * seam into an account, composing the app's other seams on the way: debits are
 * auto-categorised through the {@link ExpenseCategorizer}, and entries in a
 * foreign currency are converted through the exchange-rate seam (no rate — the
 * entry is skipped with a warning).
 *
 * <p>Entries route by sign — debits become expenses, credits become income.
 * De-duplication is content-based: an entry whose account, date, signed amount
 * and description already exist is skipped, so re-importing an overlapping
 * statement is idempotent. The bank's {@code externalId} is not persisted yet;
 * a future sync-backed implementation can keep an id ledger instead.</p>
 */
public final class BankFeedImportService {

    private final ExpenseService expenses;
    private final IncomeService incomes;
    private final CategoryService categories;
    private final ExpenseCategorizer categorizer;
    private final CurrencyConversionService conversions;
    private final Currency appCurrency;

    public BankFeedImportService(ExpenseService expenses, IncomeService incomes,
                                 CategoryService categories, ExpenseCategorizer categorizer,
                                 CurrencyConversionService conversions, Currency appCurrency) {
        this.expenses = Objects.requireNonNull(expenses, "expenses");
        this.incomes = Objects.requireNonNull(incomes, "incomes");
        this.categories = Objects.requireNonNull(categories, "categories");
        this.categorizer = Objects.requireNonNull(categorizer, "categorizer");
        this.conversions = Objects.requireNonNull(conversions, "conversions");
        this.appCurrency = Objects.requireNonNull(appCurrency, "appCurrency");
    }

    /**
     * Imports the entries into the given account without budget-alert checks.
     *
     * @return counts of imported and skipped entries plus human-readable warnings
     */
    public ImportResult importInto(long accountId, List<BankFeedEntry> entries) {
        return importInto(accountId, entries, null);
    }

    /**
     * Imports the entries into the given account. When {@code budgetAlerts} is
     * supplied, every budget affected by an imported debit is re-checked once
     * after the batch — one potential notification per (month, category), not
     * one per entry.
     *
     * @return counts of imported and skipped entries plus human-readable warnings
     */
    public ImportResult importInto(long accountId, List<BankFeedEntry> entries,
                                   BudgetAlertService budgetAlerts) {
        record AffectedBudget(YearMonth month, long categoryId) { }
        int imported = 0;
        int skipped = 0;
        List<String> warnings = new ArrayList<>();
        Set<AffectedBudget> affected = new LinkedHashSet<>();
        var expenseCategories = categories.listByType(CategoryType.EXPENSE).stream()
                .filter(c -> !c.archived()).toList();
        for (BankFeedEntry entry : entries) {
            if (entry.amount().isZero()) {
                skipped++;
                warnings.add("Zero amount skipped: " + entry.description());
                continue;
            }
            Optional<Money> inAppCurrency =
                    conversions.convert(entry.amount(), appCurrency, entry.postedOn());
            if (inAppCurrency.isEmpty()) {
                skipped++;
                warnings.add("No exchange rate for " + entry.amount().currency().getCurrencyCode()
                        + ": " + entry.description());
                continue;
            }
            Money signed = inAppCurrency.get();
            if (alreadyPresent(accountId, entry.postedOn(), signed, entry.description())) {
                skipped++;
                continue;
            }
            if (signed.isNegative()) {
                Long categoryId = categorizer.suggest(entry.description(), expenseCategories)
                        .map(CategorySuggestion::categoryId)
                        .orElse(null);
                expenses.create(new CreateExpenseRequest(accountId, categoryId, null,
                        signed.abs(), entry.description(), entry.postedOn()));
                if (categoryId != null) {
                    affected.add(new AffectedBudget(YearMonth.from(entry.postedOn()), categoryId));
                }
            } else {
                incomes.create(new CreateIncomeRequest(accountId, null,
                        signed, entry.description(), entry.postedOn()));
            }
            imported++;
        }
        if (budgetAlerts != null) {
            for (AffectedBudget budget : affected) {
                budgetAlerts.checkAfterExpenseChange(budget.month(), budget.categoryId());
            }
        }
        return new ImportResult(imported, skipped, warnings);
    }

    private boolean alreadyPresent(long accountId, LocalDate date, Money signed, String description) {
        String wanted = normalise(description);
        if (signed.isNegative()) {
            for (Expense e : expenses.listByDateRange(date, date)) {
                if (e.accountId() == accountId && e.signedAmount().equals(signed)
                        && normalise(e.description()).equals(wanted)) {
                    return true;
                }
            }
        } else {
            for (Income i : incomes.listByDateRange(date, date)) {
                if (i.accountId() == accountId && i.signedAmount().equals(signed)
                        && normalise(i.description()).equals(wanted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalise(String description) {
        return description == null ? "" : description.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
