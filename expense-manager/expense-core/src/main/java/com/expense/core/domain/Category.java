package com.expense.core.domain;

import java.util.Objects;

/**
 * Immutable category used to classify transactions. A category is scoped to a
 * {@link CategoryType} so that expense and income taxonomies stay separate.
 *
 * @param id       persistence id, {@code null} before insertion
 * @param name     unique (per type) human-readable name
 * @param type     whether this category applies to expenses or income
 * @param colorHex optional {@code #RRGGBB} colour for UI, may be {@code null}
 * @param icon     optional icon key for UI, may be {@code null}
 * @param archived whether the category is hidden from new-entry pickers
 */
public record Category(Long id, String name, CategoryType type,
                       String colorHex, String icon, boolean archived) {

    public Category {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }

    /** Factory for a fresh, non-archived category prior to persistence. */
    public static Category create(String name, CategoryType type, String colorHex, String icon) {
        return new Category(null, name, type, colorHex, icon, false);
    }

    /** @return a copy of this category with the given id assigned. */
    public Category withId(long newId) {
        return new Category(newId, name, type, colorHex, icon, archived);
    }

    /** @return a copy of this category with the archived flag toggled to {@code value}. */
    public Category withArchived(boolean value) {
        return new Category(id, name, type, colorHex, icon, value);
    }
}
