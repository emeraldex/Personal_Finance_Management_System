package com.expense.core.service;

import com.expense.core.domain.Income;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IncomeServiceTest extends CoreTestBase {

    @Test
    void createStoresIncomeAsPositiveAmount() {
        Income i = manager.incomes().create(new CreateIncomeRequest(
                account.id(), salary.id(), usd("3000.00"), "January salary", LocalDate.of(2026, 1, 1)));
        assertNotNull(i.id());
        assertEquals(usd("3000.00"), i.amount());
        assertTrue(i.amount().isPositive());
    }

    @Test
    void rejectsExpenseCategoryOnIncome() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.incomes().create(new CreateIncomeRequest(
                        account.id(), groceries.id(), usd("100.00"), "x", LocalDate.now())));
        assertTrue(ex.errors().get("categoryId").get(0).contains("INCOME"));
    }
}
