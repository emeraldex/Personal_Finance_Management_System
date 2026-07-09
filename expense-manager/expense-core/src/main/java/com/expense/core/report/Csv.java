package com.expense.core.report;

/** Minimal RFC-4180 CSV field escaping shared by the CSV exporters. */
final class Csv {

    private Csv() {
    }

    /** Quotes and escapes a field when it contains a comma, quote or newline. */
    static String field(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuoting) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    static String row(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(field(fields[i]));
        }
        return sb.append('\n').toString();
    }
}
