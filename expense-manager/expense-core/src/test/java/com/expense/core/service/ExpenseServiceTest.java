package com.expense.core.service;

import com.expense.core.domain.Expense;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.UpdateExpenseRequest;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseServiceTest extends CoreTestBase {

    @Test
    void createStoresExpenseAsNegativeAmount() {
        Expense e = manager.expenses().create(new CreateExpenseRequest(
                account.id(), groceries.id(), cash.id(), usd("42.50"), "Weekly shop", LocalDate.of(2026, 1, 5)));
        assertNotNull(e.id());
        assertEquals(usd("-42.50"), e.amount());
        assertTrue(e.amount().isNegative());
    }

    @Test
    void rejectsUnknownAccount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.expenses().create(new CreateExpenseRequest(
                        9999, groceries.id(), cash.id(), usd("10.00"), "x", LocalDate.now())));
        assertTrue(ex.errors().containsKey("accountId"));
    }

    @Test
    void rejectsIncomeCategoryOnExpense() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.expenses().create(new CreateExpenseRequest(
                        account.id(), salary.id(), cash.id(), usd("10.00"), "x", LocalDate.now())));
        assertTrue(ex.errors().get("categoryId").get(0).contains("EXPENSE"));
    }

    @Test
    void rejectsZeroAmount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.expenses().create(new CreateExpenseRequest(
                        account.id(), groceries.id(), cash.id(), usd("0.00"), "x", LocalDate.now())));
        assertTrue(ex.errors().containsKey("amount"));
    }

    @Test
    void updateChangesAmountKeepingNegativeSign() {
        Expense e = manager.expenses().create(new CreateExpenseRequest(
                account.id(), groceries.id(), cash.id(), usd("10.00"), "x", LocalDate.now()));
        Expense updated = manager.expenses().update(new UpdateExpenseRequest(
                e.id(), account.id(), groceries.id(), cash.id(), usd("25.00"), "y", LocalDate.now()));
        assertEquals(usd("-25.00"), updated.amount());
        assertEquals("y", manager.expenses().get(e.id()).description());
    }

    @Test
    void deleteRemovesExpense() {
        Expense e = manager.expenses().create(new CreateExpenseRequest(
                account.id(), groceries.id(), cash.id(), usd("10.00"), "x", LocalDate.now()));
        manager.expenses().delete(e.id());
        assertThrows(EntityNotFoundException.class, () -> manager.expenses().get(e.id()));
    }
}
