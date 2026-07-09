package com.expense.core.repository;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;

import java.util.List;
import java.util.Optional;

/** Persistence port for {@link Category} master data. */
public interface CategoryRepository {
    /** Inserts a new category and returns it with its generated id. */
    Category save(Category category);

    /** Updates an existing category (matched by id). */
    void update(Category category);

    Optional<Category> findById(long id);

    List<Category> findAll();

    /** @return all non-archived categories of the given type, ordered by name. */
    List<Category> findByType(CategoryType type);

    void deleteById(long id);
}
