package com.expense.core.report;

import java.util.List;

/**
 * Outcome of importing a legacy workbook.
 *
 * @param imported number of rows successfully imported
 * @param skipped  number of rows skipped (blank/duplicate/invalid)
 * @param warnings human-readable notes for the user
 */
public record ImportResult(int imported, int skipped, List<String> warnings) {
    public ImportResult {
        warnings = List.copyOf(warnings);
    }
}
