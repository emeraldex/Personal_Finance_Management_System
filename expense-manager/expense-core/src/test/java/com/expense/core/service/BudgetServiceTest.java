package com.expense.core.service;

import com.expense.core.domain.Budget;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetServiceTest extends CoreTestBase {

    private static final YearMonth JAN = YearMonth.of(2026, 1);

    @Test
    void setsBudgetForExpenseCategory() {
        Budget b = manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("500.00")));
        assertEquals(usd("500.00"), b.limit());
        assertEquals(JAN, b.month());
    }

    @Test
    void rejectsBudgetOnIncomeCategory() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.budgets().set(new CreateBudgetRequest(salary.id(), JAN, usd("500.00"))));
        assertTrue(ex.errors().containsKey("categoryId"));
    }

    @Test
    void upsertReplacesExistingBudgetForSameCategoryAndMonth() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("500.00")));
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("650.00")));
        List<Budget> budgets = manager.budgets().listForMonth(JAN);
        assertEquals(1, budgets.stream().filter(b -> b.categoryId() == groceries.id()).count());
        assertEquals(usd("650.00"),
                budgets.stream().filter(b -> b.categoryId() == groceries.id()).findFirst().orElseThrow().limit());
    }

    @Test
    void listForMonthReturnsOnlyThatMonth() {
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), JAN, usd("500.00")));
        manager.budgets().set(new CreateBudgetRequest(groceries.id(), YearMonth.of(2026, 2), usd("400.00")));
        assertEquals(1, manager.budgets().listForMonth(JAN).size());
    }
}
