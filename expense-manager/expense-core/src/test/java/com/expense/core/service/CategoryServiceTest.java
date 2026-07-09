package com.expense.core.service;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.exception.DuplicateEntityException;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryServiceTest extends CoreTestBase {

    @Test
    void rejectsBlankName() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                manager.categories().create(new CreateCategoryRequest(" ", CategoryType.EXPENSE, "#000", "x")));
        assertTrue(ex.errors().containsKey("name"));
    }

    @Test
    void rejectsDuplicateNameOfSameTypeCaseInsensitive() {
        assertThrows(DuplicateEntityException.class, () ->
                manager.categories().create(new CreateCategoryRequest("groceries", CategoryType.EXPENSE, "#000", "x")));
    }

    @Test
    void allowsSameNameForDifferentType() {
        Category c = manager.categories().create(
                new CreateCategoryRequest("Groceries", CategoryType.INCOME, "#000", "x"));
        assertEquals(CategoryType.INCOME, c.type());
    }

    @Test
    void renameChangesName() {
        Category renamed = manager.categories().rename(groceries.id(), "Food");
        assertEquals("Food", renamed.name());
        assertEquals("Food", manager.categories().get(groceries.id()).name());
    }

    @Test
    void archiveHidesFromDefaultButKeepsRow() {
        manager.categories().setArchived(rent.id(), true);
        assertTrue(manager.categories().get(rent.id()).archived());
    }

    @Test
    void listByTypeFiltersCorrectly() {
        List<Category> expenseCats = manager.categories().listByType(CategoryType.EXPENSE);
        assertTrue(expenseCats.stream().anyMatch(c -> c.name().equals("Groceries")));
        assertTrue(expenseCats.stream().anyMatch(c -> c.name().equals("Rent")));
        assertFalse(expenseCats.stream().anyMatch(c -> c.name().equals("Salary")));
    }

    @Test
    void getUnknownThrows() {
        assertThrows(EntityNotFoundException.class, () -> manager.categories().get(9999));
    }
}
