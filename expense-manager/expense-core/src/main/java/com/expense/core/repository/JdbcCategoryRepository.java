package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.mapper.Mappers;

import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link CategoryRepository}. */
public final class JdbcCategoryRepository extends JdbcSupport implements CategoryRepository {

    public JdbcCategoryRepository(ConnectionProvider connections) {
        super(connections);
    }

    @Override
    public Category save(Category c) {
        long id = insert(
                "INSERT INTO category(name, type, color_hex, icon, archived) VALUES (?,?,?,?,?)",
                ps -> {
                    ps.setString(1, c.name());
                    ps.setString(2, c.type().name());
                    ps.setString(3, c.colorHex());
                    ps.setString(4, c.icon());
                    ps.setInt(5, c.archived() ? 1 : 0);
                });
        return c.withId(id);
    }

    @Override
    public void update(Category c) {
        execute("UPDATE category SET name=?, type=?, color_hex=?, icon=?, archived=? WHERE id=?",
                ps -> {
                    ps.setString(1, c.name());
                    ps.setString(2, c.type().name());
                    ps.setString(3, c.colorHex());
                    ps.setString(4, c.icon());
                    ps.setInt(5, c.archived() ? 1 : 0);
                    ps.setLong(6, c.id());
                });
    }

    @Override
    public Optional<Category> findById(long id) {
        return queryOne("SELECT * FROM category WHERE id=?",
                ps -> ps.setLong(1, id), Mappers::category);
    }

    @Override
    public List<Category> findAll() {
        return query("SELECT * FROM category ORDER BY type, name", NO_ARGS, Mappers::category);
    }

    @Override
    public List<Category> findByType(CategoryType type) {
        return query("SELECT * FROM category WHERE type=? AND archived=0 ORDER BY name",
                ps -> ps.setString(1, type.name()), Mappers::category);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM category WHERE id=?", ps -> ps.setLong(1, id));
    }
}
