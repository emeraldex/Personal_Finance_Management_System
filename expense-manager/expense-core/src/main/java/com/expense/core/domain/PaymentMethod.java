package com.expense.core.domain;

import java.util.Objects;

/**
 * Immutable payment instrument (cash, a specific card, a wallet, ...).
 *
 * @param id       persistence id, {@code null} before insertion
 * @param name     unique human-readable name
 * @param type     high-level classification
 * @param archived whether hidden from new-entry pickers
 */
public record PaymentMethod(Long id, String name, PaymentMethodType type, boolean archived) {

    public PaymentMethod {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }

    public static PaymentMethod create(String name, PaymentMethodType type) {
        return new PaymentMethod(null, name, type, false);
    }

    public PaymentMethod withId(long newId) {
        return new PaymentMethod(newId, name, type, archived);
    }

    public PaymentMethod withArchived(boolean value) {
        return new PaymentMethod(id, name, type, value);
    }
}
