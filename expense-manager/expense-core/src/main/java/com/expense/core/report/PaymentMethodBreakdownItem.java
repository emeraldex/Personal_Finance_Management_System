package com.expense.core.report;

import com.expense.core.util.Money;

/**
 * One row of a payment-method breakdown of spending.
 *
 * @param paymentMethodId payment method id ({@code null} for "None")
 * @param name            display name
 * @param total           signed spend via this method
 * @param percentage      share of total spend, 0..100
 */
public record PaymentMethodBreakdownItem(Long paymentMethodId, String name,
                                         Money total, double percentage) {
}
