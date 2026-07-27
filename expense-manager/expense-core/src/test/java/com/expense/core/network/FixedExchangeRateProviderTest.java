package com.expense.core.network;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedExchangeRateProviderTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency MYR = Currency.getInstance("MYR");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final LocalDate ANY_DAY = LocalDate.of(2026, 1, 10);

    private final FixedExchangeRateProvider provider = new FixedExchangeRateProvider();

    @Test
    void returnsDirectRate() {
        provider.setRate(USD, MYR, new BigDecimal("4.70"));
        assertEquals(new BigDecimal("4.70"), provider.rate(USD, MYR, ANY_DAY).orElseThrow());
    }

    @Test
    void derivesInverseRate() {
        provider.setRate(USD, MYR, new BigDecimal("4.70"));
        BigDecimal expected = BigDecimal.ONE.divide(new BigDecimal("4.70"), 12, RoundingMode.HALF_EVEN);
        assertEquals(expected, provider.rate(MYR, USD, ANY_DAY).orElseThrow());
    }

    @Test
    void sameCurrencyIsAlwaysOne() {
        assertEquals(BigDecimal.ONE, provider.rate(MYR, MYR, ANY_DAY).orElseThrow());
    }

    @Test
    void emptyForUnknownPair() {
        assertTrue(provider.rate(EUR, MYR, ANY_DAY).isEmpty());
    }

    @Test
    void removeForgetsBothDirections() {
        provider.setRate(USD, MYR, new BigDecimal("4.70"));
        provider.removeRate(MYR, USD);
        assertTrue(provider.rate(USD, MYR, ANY_DAY).isEmpty());
        assertTrue(provider.rate(MYR, USD, ANY_DAY).isEmpty());
    }

    @Test
    void rejectsNonPositiveRate() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.setRate(USD, MYR, BigDecimal.ZERO));
    }

    @Test
    void sourcesForListsDirectAndInversePairs() {
        provider.setRate(USD, MYR, new BigDecimal("4.70"));
        provider.setRate(MYR, EUR, new BigDecimal("0.196"));
        assertEquals(Set.of(USD, EUR), provider.sourcesFor(MYR));
    }
}
