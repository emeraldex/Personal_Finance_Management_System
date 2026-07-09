package com.expense.core.dto;

import com.expense.core.domain.PaymentMethodType;

/** Command to create a payment method. */
public record CreatePaymentMethodRequest(String name, PaymentMethodType type) {
}
