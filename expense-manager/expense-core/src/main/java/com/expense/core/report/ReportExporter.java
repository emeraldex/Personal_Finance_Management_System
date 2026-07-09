package com.expense.core.report;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Strategy for serialising a report artefact of type {@code T} to a byte stream.
 * Implementations exist per output format (CSV now; Excel/PDF in a later
 * iteration behind the same interface, so callers never change).
 *
 * @param <T> the report model being exported
 */
public interface ReportExporter<T> {

    /** Writes {@code data} to {@code out}. The stream is not closed by this method. */
    void export(T data, OutputStream out) throws IOException;

    /** @return a short format identifier, e.g. {@code "csv"}, {@code "xlsx"}, {@code "pdf"}. */
    String format();
}
