package com.expense.core.service;

import com.expense.core.network.FixedExchangeRateProvider;
import com.expense.core.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyConversionServiceTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency MYR = Currency.getInstance("MYR");
    private static final Currency JPY = Currency.getInstance("JPY");
    private static final LocalDate ANY_DAY = LocalDate.of(2026, 1, 10);

    private final FixedExchangeRateProvider provider = new FixedExchangeRateProvider();
    private final CurrencyConversionService service = new CurrencyConversionService(provider);

    @Test
    void sameCurrencyIsIdentity() {
        Money amount = Money.of("25.00", MYR);
        assertEquals(amount, service.convert(amount, MYR, ANY_DAY).orElseThrow());
    }

    @Test
    void convertsUsingProviderRate() {
        provider.setRate(USD, MYR, new BigDecimal("4.70"));
        Money converted = service.convert(Money.of("25.00", USD), MYR, ANY_DAY).orElseThrow();
        assertEquals(Money.of("117.50", MYR), converted);
    }

    @Test
    void convertsBackViaDerivedInverseRate() {
        provider.setRate(USD, MYR, new BigDecimal("4.00"));
        Money converted = service.convert(Money.of("100.00", MYR), USD, ANY_DAY).orElseThrow();
        assertEquals(Money.of("25.00", USD), converted);
    }

    @Test
    void emptyWhenNoRateAvailable() {
        assertTrue(service.convert(Money.of("25.00", USD), MYR, ANY_DAY).isEmpty());
    }

    @Test
    void resultUsesTargetCurrencyFractionDigits() {
        provider.setRate(USD, JPY, new BigDecimal("150"));
        Money converted = service.convert(Money.of("12.34", USD), JPY, ANY_DAY).orElseThrow();
        assertEquals(Money.of("1851", JPY), converted);
        assertEquals(0, converted.amount().scale());
    }
}
