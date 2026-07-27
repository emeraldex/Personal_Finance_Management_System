package com.expense.core.network;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Offline default {@link ExchangeRateProvider}: a user-maintained table of
 * fixed rates, the FX counterpart of {@link HeuristicExpenseCategorizer}. A
 * pair set one way also serves the inverse direction, and the date is ignored
 * because a fixed rate has no history. An API-backed provider with dated rates
 * can replace this behind the same seam.
 */
public final class FixedExchangeRateProvider implements ExchangeRateProvider {

    private static final int INVERSE_SCALE = 12;

    /** Keyed "FROM->TO" by ISO code; value is units of TO per one unit of FROM. */
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();

    /**
     * Sets (or replaces) the fixed rate: one unit of {@code from} equals
     * {@code rate} units of {@code to}. The inverse direction is derived.
     *
     * @throws IllegalArgumentException when the rate is not positive
     */
    public void setRate(Currency from, Currency to, BigDecimal rate) {
        if (rate.signum() <= 0) {
            throw new IllegalArgumentException("rate must be positive: " + rate);
        }
        rates.put(key(from, to), rate);
    }

    /** Forgets the pair, whichever direction it was set in. */
    public void removeRate(Currency from, Currency to) {
        rates.remove(key(from, to));
        rates.remove(key(to, from));
    }

    /** Currencies convertible (directly or by inverse) into the given quote currency. */
    public Set<Currency> sourcesFor(Currency to) {
        Set<Currency> sources = new TreeSet<>((a, b) -> a.getCurrencyCode().compareTo(b.getCurrencyCode()));
        String quote = to.getCurrencyCode();
        for (String key : rates.keySet()) {
            String[] pair = key.split("->", 2);
            if (pair[1].equals(quote)) {
                sources.add(Currency.getInstance(pair[0]));
            } else if (pair[0].equals(quote)) {
                sources.add(Currency.getInstance(pair[1]));
            }
        }
        return sources;
    }

    @Override
    public Optional<BigDecimal> rate(Currency from, Currency to, LocalDate on) {
        if (from.equals(to)) {
            return Optional.of(BigDecimal.ONE);
        }
        BigDecimal direct = rates.get(key(from, to));
        if (direct != null) {
            return Optional.of(direct);
        }
        BigDecimal inverse = rates.get(key(to, from));
        if (inverse != null) {
            return Optional.of(BigDecimal.ONE.divide(inverse, INVERSE_SCALE, RoundingMode.HALF_EVEN));
        }
        return Optional.empty();
    }

    private static String key(Currency from, Currency to) {
        return from.getCurrencyCode() + "->" + to.getCurrencyCode();
    }
}
