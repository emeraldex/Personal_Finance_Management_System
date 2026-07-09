package com.expense.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void addsAndSubtractsWithoutFloatingPointDrift() {
        Money a = Money.of("0.10", USD);
        Money b = Money.of("0.20", USD);
        assertEquals(Money.of("0.30", USD), a.add(b));
        assertEquals(Money.of("-0.10", USD), a.subtract(b));
    }

    @Test
    void convertsToAndFromMinorUnits() {
        assertEquals(1250L, Money.of("12.50", USD).toMinor());
        assertEquals(Money.of("12.50", USD), Money.ofMinor(1250, USD));
    }

    @Test
    void roundsHalfEvenToCurrencyScale() {
        assertEquals(Money.of("2.12", USD), Money.of("2.125", USD)); // banker's rounding
        assertEquals(Money.of("2.14", USD), Money.of("2.135", USD));
    }

    @Test
    void reportsSignCorrectly() {
        assertTrue(Money.of("-1.00", USD).isNegative());
        assertTrue(Money.of("1.00", USD).isPositive());
        assertTrue(Money.zero(USD).isZero());
        assertEquals(Money.of("1.00", USD), Money.of("-1.00", USD).abs());
        assertEquals(Money.of("-5.00", USD), Money.of("5.00", USD).negate());
    }

    @Test
    void rejectsCurrencyMismatch() {
        Money usd = Money.of("1.00", USD);
        Money eur = Money.of("1.00", Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
    }

    @Test
    void multipliesByFactor() {
        assertEquals(Money.of("30.00", USD), Money.of("10.00", USD).multiply(new BigDecimal("3")));
    }

    @Test
    void equalityIgnoresTrailingZeros() {
        assertEquals(Money.of("5", USD), Money.of("5.00", USD));
    }
}
