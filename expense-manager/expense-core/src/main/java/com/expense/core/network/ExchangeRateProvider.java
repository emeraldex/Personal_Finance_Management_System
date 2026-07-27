package com.expense.core.network;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

/**
 * Seam for foreign-exchange rates, the missing piece for multi-currency
 * support. {@code Money} deliberately rejects cross-currency arithmetic, so
 * converting a foreign-currency expense into the app currency requires a rate
 * from outside the core. A concrete implementation (a rates API, a cached
 * table, a fixed user-entered rate) is injected by an outer module; returning
 * {@link Optional#empty()} keeps the app functional while offline.
 */
public interface ExchangeRateProvider {
    /**
     * Returns the rate to multiply an amount in {@code from} by to obtain the
     * equivalent amount in {@code to}, as of the given date.
     *
     * @param from the currency the amount is denominated in
     * @param to   the currency to convert into
     * @param on   the date whose closing rate applies (transaction date)
     * @return the conversion rate, or empty when no rate is available
     */
    Optional<BigDecimal> rate(Currency from, Currency to, LocalDate on);
}
