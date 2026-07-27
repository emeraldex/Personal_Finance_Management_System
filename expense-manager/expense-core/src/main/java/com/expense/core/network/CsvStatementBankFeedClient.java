package com.expense.core.network;

import com.expense.core.exception.PersistenceException;
import com.expense.core.util.Money;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Currency;

/**
 * Offline default {@link BankFeedClient}: parses a bank-statement CSV file
 * downloaded from online banking — the statement-parser implementation the
 * seam's contract anticipates. The {@code linkedAccountRef} is the path of the
 * statement file.
 *
 * <p>Expected columns: {@code Date,Description,Amount[,ExternalId]}. A header
 * row and rows with unparseable dates or amounts are skipped. Dates are ISO
 * ({@code yyyy-MM-dd}); amounts are signed decimals in the statement currency
 * following the project convention (debits negative, credits positive). When
 * the bank provides no transaction id, one is synthesised deterministically
 * from the row's content (with a counter for identical rows) so re-parsing the
 * same statement yields the same ids.</p>
 */
public final class CsvStatementBankFeedClient implements BankFeedClient {

    private final Currency statementCurrency;

    /** @param statementCurrency the currency amounts in the statement are denominated in */
    public CsvStatementBankFeedClient(Currency statementCurrency) {
        this.statementCurrency = statementCurrency;
    }

    @Override
    public List<BankFeedEntry> fetch(String linkedAccountRef, LocalDate from, LocalDate to) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(linkedAccountRef), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Failed to read statement: " + linkedAccountRef, e);
        }
        List<BankFeedEntry> entries = new ArrayList<>();
        Map<String, Integer> duplicateCounter = new HashMap<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            List<String> cells = parseCsvLine(line);
            if (cells.size() < 3) {
                continue;
            }
            LocalDate date = parseDate(cells.get(0));
            BigDecimal amount = parseAmount(cells.get(2));
            if (date == null || amount == null) {
                continue; // header or malformed row
            }
            if (date.isBefore(from) || date.isAfter(to)) {
                continue;
            }
            String description = cells.get(1).trim();
            String externalId = cells.size() >= 4 && !cells.get(3).isBlank()
                    ? cells.get(3).trim()
                    : synthesiseId(date, description, amount, duplicateCounter);
            entries.add(new BankFeedEntry(date, description,
                    Money.of(amount, statementCurrency), externalId));
        }
        entries.sort(Comparator.comparing(BankFeedEntry::postedOn));
        return entries;
    }

    private static String synthesiseId(LocalDate date, String description, BigDecimal amount,
                                       Map<String, Integer> duplicateCounter) {
        String base = date + "|" + description + "|" + amount.toPlainString();
        int occurrence = duplicateCounter.merge(base, 1, Integer::sum);
        return occurrence == 1 ? base : base + "|" + occurrence;
    }

    private static LocalDate parseDate(String cell) {
        try {
            return LocalDate.parse(cell.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal parseAmount(String cell) {
        try {
            return new BigDecimal(cell.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Minimal RFC-4180-style field split: quoted fields may contain commas and doubled quotes. */
    private static List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString());
        return cells;
    }
}
