package com.expense.core.service;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.exception.DuplicateEntityException;
import com.expense.core.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentMethodAndAccountServiceTest extends CoreTestBase {

    @Test
    void createsPaymentMethod() {
        PaymentMethod pm = manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Visa", PaymentMethodType.CREDIT_CARD));
        assertNotNull(pm.id());
        assertEquals(PaymentMethodType.CREDIT_CARD, pm.type());
    }

    @Test
    void rejectsDuplicatePaymentMethodName() {
        assertThrows(DuplicateEntityException.class, () ->
                manager.paymentMethods().create(new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH)));
    }

    @Test
    void archivesPaymentMethod() {
        PaymentMethod pm = manager.paymentMethods().setArchived(cash.id(), true);
        assertTrue(pm.archived());
    }

    @Test
    void createsAccountWithOpeningBalance() {
        Account a = manager.accounts().create(
                new CreateAccountRequest("Savings", AccountType.SAVINGS, usd("1000.00")));
        assertNotNull(a.id());
        assertEquals(usd("1000.00"), a.openingBalance());
    }

    @Test
    void rejectsDuplicateAccountName() {
        assertThrows(DuplicateEntityException.class, () ->
                manager.accounts().create(new CreateAccountRequest("Checking", AccountType.CHECKING, usd("0.00"))));
    }

    @Test
    void deleteUnknownAccountThrows() {
        assertThrows(EntityNotFoundException.class, () -> manager.accounts().delete(9999));
    }
}
