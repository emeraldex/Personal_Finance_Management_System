package com.expense.core.service;

import com.expense.core.network.ExchangeRateProvider;
import com.expense.core.util.Money;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

/**
 * Converts {@link Money} between currencies over the {@link ExchangeRateProvider}
 * seam. {@code Money} itself deliberately rejects cross-currency arithmetic, so
 * this service is the one sanctioned crossing point: it looks up the rate,
 * multiplies, and re-normalises to the target currency's fraction digits.
 * Returning empty (no rate available) keeps callers functional while offline.
 */
public final class CurrencyConversionService {

    private final ExchangeRateProvider rates;

    public CurrencyConversionService(ExchangeRateProvider rates) {
        this.rates = Objects.requireNonNull(rates, "rates");
    }

    /**
     * Converts the amount into the target currency at the rate for the given date.
     *
     * @param amount the amount to convert
     * @param to     the target currency; same-currency conversion is the identity
     * @param on     the date whose rate applies (transaction date)
     * @return the converted amount, or empty when no rate is available
     */
    public Optional<Money> convert(Money amount, Currency to, LocalDate on) {
        if (amount.currency().equals(to)) {
            return Optional.of(amount);
        }
        return rates.rate(amount.currency(), to, on)
                .map(rate -> Money.of(amount.amount().multiply(rate), to));
    }
}
