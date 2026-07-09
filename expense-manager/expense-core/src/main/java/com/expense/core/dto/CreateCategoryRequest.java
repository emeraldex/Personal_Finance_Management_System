package com.expense.core.dto;

import com.expense.core.domain.CategoryType;

/** Command to create a category. */
public record CreateCategoryRequest(String name, CategoryType type, String colorHex, String icon) {
}
