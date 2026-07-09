package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.mapper.Mappers;

import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link PaymentMethodRepository}. */
public final class JdbcPaymentMethodRepository extends JdbcSupport implements PaymentMethodRepository {

    public JdbcPaymentMethodRepository(ConnectionProvider connections) {
        super(connections);
    }

    @Override
    public PaymentMethod save(PaymentMethod p) {
        long id = insert("INSERT INTO payment_method(name, type, archived) VALUES (?,?,?)",
                ps -> {
                    ps.setString(1, p.name());
                    ps.setString(2, p.type().name());
                    ps.setInt(3, p.archived() ? 1 : 0);
                });
        return p.withId(id);
    }

    @Override
    public void update(PaymentMethod p) {
        execute("UPDATE payment_method SET name=?, type=?, archived=? WHERE id=?",
                ps -> {
                    ps.setString(1, p.name());
                    ps.setString(2, p.type().name());
                    ps.setInt(3, p.archived() ? 1 : 0);
                    ps.setLong(4, p.id());
                });
    }

    @Override
    public Optional<PaymentMethod> findById(long id) {
        return queryOne("SELECT * FROM payment_method WHERE id=?",
                ps -> ps.setLong(1, id), Mappers::paymentMethod);
    }

    @Override
    public List<PaymentMethod> findAll() {
        return query("SELECT * FROM payment_method ORDER BY name", NO_ARGS, Mappers::paymentMethod);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM payment_method WHERE id=?", ps -> ps.setLong(1, id));
    }
}
