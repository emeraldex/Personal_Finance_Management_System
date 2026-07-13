package com.expense.android.data;

import com.expense.core.domain.Account;
import com.expense.core.repository.AccountRepository;

import java.util.List;
import java.util.Optional;

/** {@code android.database.sqlite} implementation of {@link AccountRepository}. */
public final class AndroidAccountRepository extends AndroidSqlSupport implements AccountRepository {

    public AndroidAccountRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public Account save(Account a) {
        long id = insert(
                "INSERT INTO account(name, type, opening_balance_minor, currency, archived) VALUES (?,?,?,?,?)",
                a.name(), a.type().name(), a.openingBalance().toMinor(),
                a.openingBalance().currency().getCurrencyCode(), a.archived() ? 1 : 0);
        return a.withId(id);
    }

    @Override
    public void update(Account a) {
        execute("UPDATE account SET name=?, type=?, opening_balance_minor=?, currency=?, archived=? WHERE id=?",
                a.name(), a.type().name(), a.openingBalance().toMinor(),
                a.openingBalance().currency().getCurrencyCode(), a.archived() ? 1 : 0, a.id());
    }

    @Override
    public Optional<Account> findById(long id) {
        return queryOne("SELECT * FROM account WHERE id=?", args(id), AndroidMappers::account);
    }

    @Override
    public List<Account> findAll() {
        return query("SELECT * FROM account ORDER BY name", null, AndroidMappers::account);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM account WHERE id=?", id);
    }
}
