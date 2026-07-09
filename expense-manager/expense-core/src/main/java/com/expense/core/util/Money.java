package com.expense.core.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable monetary value backed by {@link BigDecimal} to avoid binary
 * floating-point rounding errors that are unacceptable in financial software.
 *
 * <p>Every {@code Money} carries an ISO-4217 {@link Currency}. Arithmetic between
 * two {@code Money} instances of different currencies is rejected. Amounts are
 * normalised to the currency's default fraction digits (2 for most currencies)
 * using {@link RoundingMode#HALF_EVEN} (banker's rounding).</p>
 *
 * <p>By project convention, expenses are represented as negative amounts and
 * income as positive amounts. {@code Money} itself is sign-agnostic and simply
 * stores whatever signed value it is given.</p>
 */
public final class Money implements Comparable<Money>, Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency");
        this.amount = Objects.requireNonNull(amount, "amount")
                .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    /** Creates a {@code Money} from a {@link BigDecimal} major-unit amount. */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /** Creates a {@code Money} from a decimal major-unit amount (e.g. {@code 12.50}). */
    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /** Creates a {@code Money} from a string amount (e.g. {@code "12.50"}). */
    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /**
     * Creates a {@code Money} from an integer number of minor units (e.g. cents).
     * This is the canonical persisted representation.
     */
    public static Money ofMinor(long minorUnits, Currency currency) {
        BigDecimal major = BigDecimal.valueOf(minorUnits)
                .movePointLeft(currency.getDefaultFractionDigits());
        return new Money(major, currency);
    }

    /** A zero amount in the given currency. */
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /** @return the major-unit amount (e.g. {@code 12.50}). */
    public BigDecimal amount() {
        return amount;
    }

    /** @return the currency of this amount. */
    public Currency currency() {
        return currency;
    }

    /** @return this amount expressed as whole minor units (e.g. cents), for persistence. */
    public long toMinor() {
        return amount.movePointRight(currency.getDefaultFractionDigits())
                .setScale(0, RoundingMode.HALF_EVEN)
                .longValueExact();
    }

    /** @return the sum of this and {@code other}. */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /** @return this amount minus {@code other}. */
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /** @return this amount scaled by {@code factor}. */
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    /** @return the arithmetic negation of this amount. */
    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    /** @return the absolute (non-negative) value of this amount. */
    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency.getCurrencyCode()
                            + " vs " + other.currency.getCurrencyCode());
        }
    }

    @Override
    public int compareTo(Money o) {
        requireSameCurrency(o);
        return amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
