package com.expense.core.domain;

/** Distinguishes categories usable for spending versus earning. */
public enum CategoryType {
    /** Category applicable to expenses (e.g. Groceries, Rent). */
    EXPENSE,
    /** Category applicable to income (e.g. Salary, Dividends). */
    INCOME
}
