package com.expense.core.service;

import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.util.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class TransactionSignTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void expenseIsAlwaysStoredNegativeRegardlessOfInputSign() {
        Expense fromPositive = Expense.create(1, null, null, Money.of("50.00", USD), "x", LocalDate.now());
        Expense fromNegative = new Expense(null, 1, null, null,
                Money.of("-50.00", USD), "x", LocalDate.now(), java.time.Instant.now());
        assertTrue(fromPositive.amount().isNegative());
        assertTrue(fromNegative.amount().isNegative());
        assertEquals(Money.of("-50.00", USD), fromPositive.signedAmount());
    }

    @Test
    void incomeIsAlwaysStoredPositiveRegardlessOfInputSign() {
        Income fromNegative = new Income(null, 1, null,
                Money.of("-50.00", USD), "x", LocalDate.now(), java.time.Instant.now());
        assertTrue(fromNegative.amount().isPositive());
        assertEquals(Money.of("50.00", USD), fromNegative.signedAmount());
    }
}
