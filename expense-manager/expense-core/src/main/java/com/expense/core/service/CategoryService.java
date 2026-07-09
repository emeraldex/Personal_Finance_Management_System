package com.expense.core.service;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.exception.DuplicateEntityException;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.validation.ValidationErrors;

import java.util.List;
import java.util.Objects;

/**
 * Application service for category management. Enforces uniqueness of
 * (name, type) and non-blank names on top of the repository.
 */
public final class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Creates a category.
     *
     * @throws com.expense.core.exception.ValidationException if the name is blank
     * @throws DuplicateEntityException if a category with the same name and type exists
     */
    public Category create(CreateCategoryRequest request) {
        ValidationErrors errors = new ValidationErrors();
        errors.addIf(isBlank(request.name()), "name", "Name must not be blank");
        errors.addIf(request.type() == null, "type", "Type is required");
        errors.throwIfInvalid();

        boolean exists = repository.findAll().stream()
                .anyMatch(c -> c.type() == request.type()
                        && c.name().equalsIgnoreCase(request.name().strip()));
        if (exists) {
            throw new DuplicateEntityException(
                    "Category already exists: " + request.name() + " (" + request.type() + ")");
        }

        Category category = Category.create(
                request.name().strip(), request.type(), request.colorHex(), request.icon());
        return repository.save(category);
    }

    /** Renames a category, keeping uniqueness. */
    public Category rename(long id, String newName) {
        Category existing = require(id);
        if (isBlank(newName)) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        Category updated = new Category(existing.id(), newName.strip(), existing.type(),
                existing.colorHex(), existing.icon(), existing.archived());
        repository.update(updated);
        return updated;
    }

    /** Archives or unarchives a category (hides it from pickers without deleting history). */
    public Category setArchived(long id, boolean archived) {
        Category existing = require(id);
        Category updated = existing.withArchived(archived);
        repository.update(updated);
        return updated;
    }

    /** Deletes a category. Existing transactions keep their history (category_id becomes NULL). */
    public void delete(long id) {
        require(id);
        repository.deleteById(id);
    }

    public Category get(long id) {
        return require(id);
    }

    public List<Category> list() {
        return repository.findAll();
    }

    public List<Category> listByType(CategoryType type) {
        return repository.findByType(type);
    }

    private Category require(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
