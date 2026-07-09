package com.expense.core.service;

import com.expense.core.domain.Account;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.exception.DuplicateEntityException;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.repository.AccountRepository;
import com.expense.core.validation.ValidationErrors;

import java.util.List;
import java.util.Objects;

/** Application service for account management. */
public final class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Account create(CreateAccountRequest request) {
        ValidationErrors errors = new ValidationErrors();
        errors.addIf(request.name() == null || request.name().isBlank(), "name", "Name must not be blank");
        errors.addIf(request.type() == null, "type", "Type is required");
        errors.addIf(request.openingBalance() == null, "openingBalance", "Opening balance is required");
        errors.throwIfInvalid();

        boolean exists = repository.findAll().stream()
                .anyMatch(a -> a.name().equalsIgnoreCase(request.name().strip()));
        if (exists) {
            throw new DuplicateEntityException("Account already exists: " + request.name());
        }
        return repository.save(
                Account.create(request.name().strip(), request.type(), request.openingBalance()));
    }

    public Account setArchived(long id, boolean archived) {
        Account existing = require(id);
        Account updated = existing.withArchived(archived);
        repository.update(updated);
        return updated;
    }

    /**
     * Deletes an account. The database rejects deletion of accounts that still
     * own transactions (ON DELETE RESTRICT), surfaced as a
     * {@link com.expense.core.exception.PersistenceException}.
     */
    public void delete(long id) {
        require(id);
        repository.deleteById(id);
    }

    public Account get(long id) {
        return require(id);
    }

    public List<Account> list() {
        return repository.findAll();
    }

    private Account require(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account", id));
    }
}
