package com.expense.core.service;

import com.expense.core.domain.PaymentMethod;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.exception.DuplicateEntityException;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.repository.PaymentMethodRepository;
import com.expense.core.validation.ValidationErrors;

import java.util.List;
import java.util.Objects;

/** Application service for payment-method management. */
public final class PaymentMethodService {

    private final PaymentMethodRepository repository;

    public PaymentMethodService(PaymentMethodRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public PaymentMethod create(CreatePaymentMethodRequest request) {
        ValidationErrors errors = new ValidationErrors();
        errors.addIf(request.name() == null || request.name().isBlank(), "name", "Name must not be blank");
        errors.addIf(request.type() == null, "type", "Type is required");
        errors.throwIfInvalid();

        boolean exists = repository.findAll().stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(request.name().strip()));
        if (exists) {
            throw new DuplicateEntityException("Payment method already exists: " + request.name());
        }
        return repository.save(PaymentMethod.create(request.name().strip(), request.type()));
    }

    public PaymentMethod setArchived(long id, boolean archived) {
        PaymentMethod existing = require(id);
        PaymentMethod updated = existing.withArchived(archived);
        repository.update(updated);
        return updated;
    }

    public void delete(long id) {
        require(id);
        repository.deleteById(id);
    }

    public PaymentMethod get(long id) {
        return require(id);
    }

    public List<PaymentMethod> list() {
        return repository.findAll();
    }

    private PaymentMethod require(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PaymentMethod", id));
    }
}
