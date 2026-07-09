package com.expense.core.repository;

import com.expense.core.domain.Account;

import java.util.List;
import java.util.Optional;

/** Persistence port for {@link Account} master data. */
public interface AccountRepository {
    Account save(Account account);

    void update(Account account);

    Optional<Account> findById(long id);

    List<Account> findAll();

    void deleteById(long id);
}
