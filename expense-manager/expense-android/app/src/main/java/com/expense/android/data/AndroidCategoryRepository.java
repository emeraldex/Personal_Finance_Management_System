package com.expense.android.data;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

/** {@code android.database.sqlite} implementation of {@link CategoryRepository}. */
public final class AndroidCategoryRepository extends AndroidSqlSupport implements CategoryRepository {

    public AndroidCategoryRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public Category save(Category c) {
        long id = insert(
                "INSERT INTO category(name, type, color_hex, icon, archived) VALUES (?,?,?,?,?)",
                c.name(), c.type().name(), c.colorHex(), c.icon(), c.archived() ? 1 : 0);
        return c.withId(id);
    }

    @Override
    public void update(Category c) {
        execute("UPDATE category SET name=?, type=?, color_hex=?, icon=?, archived=? WHERE id=?",
                c.name(), c.type().name(), c.colorHex(), c.icon(), c.archived() ? 1 : 0, c.id());
    }

    @Override
    public Optional<Category> findById(long id) {
        return queryOne("SELECT * FROM category WHERE id=?", args(id), AndroidMappers::category);
    }

    @Override
    public List<Category> findAll() {
        return query("SELECT * FROM category ORDER BY type, name", null, AndroidMappers::category);
    }

    @Override
    public List<Category> findByType(CategoryType type) {
        return query("SELECT * FROM category WHERE type=? AND archived=0 ORDER BY name",
                args(type.name()), AndroidMappers::category);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM category WHERE id=?", id);
    }
}
