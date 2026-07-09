package com.expense.core.report;

import java.io.IOException;
import java.io.InputStream;

/**
 * Seam for importing legacy Excel monthly-expense workbooks. The concrete
 * Apache-POI-backed implementation is delivered in a later iteration behind this
 * interface, so business code that triggers an import never changes. It preserves
 * data integrity by validating and normalising every row through the same
 * services used for manual entry.
 */
public interface WorkbookImporter {
    /** Reads and imports the workbook from {@code in}, returning a summary of what happened. */
    ImportResult importWorkbook(InputStream in) throws IOException;
}
