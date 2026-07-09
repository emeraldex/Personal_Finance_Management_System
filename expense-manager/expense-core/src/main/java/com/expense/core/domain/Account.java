package com.expense.core.domain;

import com.expense.core.util.Money;

import java.util.Objects;

/**
 * Immutable financial account holding an opening balance in a single currency.
 * Running balances are computed by summing signed transactions on top of the
 * opening balance rather than being mutated in place.
 *
 * @param id             persistence id, {@code null} before insertion
 * @param name           unique human-readable name
 * @param type           account classification
 * @param openingBalance starting balance (may be negative for liabilities)
 * @param archived       whether hidden from pickers
 */
public record Account(Long id, String name, AccountType type,
                      Money openingBalance, boolean archived) {

    public Account {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(openingBalance, "openingBalance");
    }

    public static Account create(String name, AccountType type, Money openingBalance) {
        return new Account(null, name, type, openingBalance, false);
    }

    public Account withId(long newId) {
        return new Account(newId, name, type, openingBalance, archived);
    }

    public Account withArchived(boolean value) {
        return new Account(id, name, type, openingBalance, value);
    }
}
