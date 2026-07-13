package com.expense.android.data;

import com.expense.core.domain.PaymentMethod;
import com.expense.core.repository.PaymentMethodRepository;

import java.util.List;
import java.util.Optional;

/** {@code android.database.sqlite} implementation of {@link PaymentMethodRepository}. */
public final class AndroidPaymentMethodRepository extends AndroidSqlSupport implements PaymentMethodRepository {

    public AndroidPaymentMethodRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public PaymentMethod save(PaymentMethod p) {
        long id = insert("INSERT INTO payment_method(name, type, archived) VALUES (?,?,?)",
                p.name(), p.type().name(), p.archived() ? 1 : 0);
        return p.withId(id);
    }

    @Override
    public void update(PaymentMethod p) {
        execute("UPDATE payment_method SET name=?, type=?, archived=? WHERE id=?",
                p.name(), p.type().name(), p.archived() ? 1 : 0, p.id());
    }

    @Override
    public Optional<PaymentMethod> findById(long id) {
        return queryOne("SELECT * FROM payment_method WHERE id=?", args(id), AndroidMappers::paymentMethod);
    }

    @Override
    public List<PaymentMethod> findAll() {
        return query("SELECT * FROM payment_method ORDER BY name", null, AndroidMappers::paymentMethod);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM payment_method WHERE id=?", id);
    }
}
