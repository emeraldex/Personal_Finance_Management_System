package com.expense.core.report;

/**
 * Marker specialisation of {@link ReportExporter} for spreadsheet output. The
 * POI-backed {@code xlsx} implementation is delivered in a later iteration; it
 * plugs in wherever a {@code ReportExporter<MonthlySummary>} is expected.
 */
public interface WorkbookExporter extends ReportExporter<MonthlySummary> {
}
