package com.expense.core.dto;

import com.expense.core.domain.AccountType;
import com.expense.core.util.Money;

/** Command to create an account with an opening balance. */
public record CreateAccountRequest(String name, AccountType type, Money openingBalance) {
}
