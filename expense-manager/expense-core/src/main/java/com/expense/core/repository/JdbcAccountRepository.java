package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.Account;
import com.expense.core.mapper.Mappers;

import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link AccountRepository}. */
public final class JdbcAccountRepository extends JdbcSupport implements AccountRepository {

    public JdbcAccountRepository(ConnectionProvider connections) {
        super(connections);
    }

    @Override
    public Account save(Account a) {
        long id = insert(
                "INSERT INTO account(name, type, opening_balance_minor, currency, archived) VALUES (?,?,?,?,?)",
                ps -> {
                    ps.setString(1, a.name());
                    ps.setString(2, a.type().name());
                    ps.setLong(3, a.openingBalance().toMinor());
                    ps.setString(4, a.openingBalance().currency().getCurrencyCode());
                    ps.setInt(5, a.archived() ? 1 : 0);
                });
        return a.withId(id);
    }

    @Override
    public void update(Account a) {
        execute("UPDATE account SET name=?, type=?, opening_balance_minor=?, currency=?, archived=? WHERE id=?",
                ps -> {
                    ps.setString(1, a.name());
                    ps.setString(2, a.type().name());
                    ps.setLong(3, a.openingBalance().toMinor());
                    ps.setString(4, a.openingBalance().currency().getCurrencyCode());
                    ps.setInt(5, a.archived() ? 1 : 0);
                    ps.setLong(6, a.id());
                });
    }

    @Override
    public Optional<Account> findById(long id) {
        return queryOne("SELECT * FROM account WHERE id=?",
                ps -> ps.setLong(1, id), Mappers::account);
    }

    @Override
    public List<Account> findAll() {
        return query("SELECT * FROM account ORDER BY name", NO_ARGS, Mappers::account);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM account WHERE id=?", ps -> ps.setLong(1, id));
    }
}
