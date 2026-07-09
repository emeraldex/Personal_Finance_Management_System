package com.expense.core.repository;

import com.expense.core.domain.PaymentMethod;

import java.util.List;
import java.util.Optional;

/** Persistence port for {@link PaymentMethod} master data. */
public interface PaymentMethodRepository {
    PaymentMethod save(PaymentMethod paymentMethod);

    void update(PaymentMethod paymentMethod);

    Optional<PaymentMethod> findById(long id);

    List<PaymentMethod> findAll();

    void deleteById(long id);
}
