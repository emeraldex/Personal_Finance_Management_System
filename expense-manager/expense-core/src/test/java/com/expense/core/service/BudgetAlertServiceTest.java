package com.expense.core.service;

import com.expense.core.domain.Category;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.network.AppNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetAlertServiceTest extends CoreTestBase {

    private static final YearMonth JAN = YearMonth.of(2026, 1);

    private final List<AppNotification> published = new ArrayList<>();
    private BudgetAlertService alerts;

    @BeforeEach
    void initAlerts() {
        alerts = new BudgetAlertService(manager.summaries(), published::add);
    }

    private void expense(Category category, String amount) {
        manager.expenses().create(new CreateExpenseRequest(
                account.id(), category.id(), cash.id(), usd(amount), "spend", JAN.atDay(10)));
    }

    @Test
    void publishesAlertWhenBudgetExceeded() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        expense(groceries, "120.00");
        Optional<AppNotification> result = alerts.checkAfterExpenseChange(JAN, groceries.id());
        assertTrue(result.isPresent());
        assertEquals(AppNotification.Severity.ALERT, result.get().severity());
        assertEquals(List.of(result.get()), published);
    }

    @Test
    void publishesWarningWhenApproachingLimit() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        expense(groceries, "95.00");
        Optional<AppNotification> result = alerts.checkAfterExpenseChange(JAN, groceries.id());
        assertTrue(result.isPresent());
        assertEquals(AppNotification.Severity.WARNING, result.get().severity());
    }

    @Test
    void silentWhenWellUnderBudget() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        expense(groceries, "50.00");
        assertTrue(alerts.checkAfterExpenseChange(JAN, groceries.id()).isEmpty());
        assertTrue(published.isEmpty());
    }

    @Test
    void silentForCategoryWithoutBudget() {
        expense(rent, "500.00");
        assertTrue(alerts.checkAfterExpenseChange(JAN, rent.id()).isEmpty());
        assertTrue(published.isEmpty());
    }

    @Test
    void silentForUncategorisedExpense() {
        assertTrue(alerts.checkAfterExpenseChange(JAN, null).isEmpty());
        assertTrue(published.isEmpty());
    }

    @Test
    void alertNamesTheCategoryAndBudget() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        expense(groceries, "120.00");
        AppNotification n = alerts.checkAfterExpenseChange(JAN, groceries.id()).orElseThrow();
        assertTrue(n.body().contains("Groceries"));
        assertTrue(n.body().contains("USD 100.00"));
    }

    @Test
    void customThresholdIsRespected() {
        BudgetAlertService strict = new BudgetAlertService(manager.summaries(), published::add, 50.0);
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("100.00")));
        expense(groceries, "60.00");
        AppNotification n = strict.checkAfterExpenseChange(JAN, groceries.id()).orElseThrow();
        assertEquals(AppNotification.Severity.WARNING, n.severity());
    }
}
