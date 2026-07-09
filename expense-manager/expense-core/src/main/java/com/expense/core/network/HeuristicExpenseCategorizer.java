package com.expense.core.network;

import com.expense.core.domain.Category;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Offline keyword-matching implementation of {@link ExpenseCategorizer}. It
 * scores each candidate category by counting case-insensitive token overlaps
 * between the description and the category name, normalised to a 0..1 confidence.
 * This provides a genuinely useful default today and a drop-in replacement point
 * for a smarter model tomorrow.
 */
public final class HeuristicExpenseCategorizer implements ExpenseCategorizer {

    private final double minConfidence;

    /** Uses a default confidence threshold of 0.25. */
    public HeuristicExpenseCategorizer() {
        this(0.25);
    }

    public HeuristicExpenseCategorizer(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    @Override
    public Optional<CategorySuggestion> suggest(String description, List<Category> candidates) {
        if (description == null || description.isBlank() || candidates.isEmpty()) {
            return Optional.empty();
        }
        String[] words = description.toLowerCase(Locale.ROOT).split("\\W+");
        CategorySuggestion best = null;
        for (Category category : candidates) {
            if (category.id() == null) {
                continue;
            }
            String catName = category.name().toLowerCase(Locale.ROOT);
            int hits = 0;
            for (String word : words) {
                if (!word.isBlank() && catName.contains(word)) {
                    hits++;
                }
            }
            double confidence = words.length == 0 ? 0.0 : (double) hits / words.length;
            if (confidence >= minConfidence && (best == null || confidence > best.confidence())) {
                best = new CategorySuggestion(category.id(), confidence);
            }
        }
        return Optional.ofNullable(best);
    }
}
