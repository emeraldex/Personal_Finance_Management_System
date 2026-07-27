package com.expense.core.network;

import com.expense.core.exception.PersistenceException;
import com.expense.core.util.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvStatementBankFeedClientTest {

    private static final Currency MYR = Currency.getInstance("MYR");
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 31);

    private final CsvStatementBankFeedClient client = new CsvStatementBankFeedClient(MYR);

    @TempDir
    Path dir;

    private String statement(String content) throws IOException {
        Path file = dir.resolve("statement.csv");
        Files.writeString(file, content);
        return file.toString();
    }

    @Test
    void parsesRowsSkippingHeaderAndSortsOldestFirst() throws IOException {
        String path = statement("""
                Date,Description,Amount
                2026-01-15,COFFEE BAR,-12.50
                2026-01-10,SALARY JAN,3000.00
                """);
        List<BankFeedEntry> entries = client.fetch(path, FROM, TO);
        assertEquals(2, entries.size());
        assertEquals(LocalDate.of(2026, 1, 10), entries.get(0).postedOn());
        assertEquals(Money.of("3000.00", MYR), entries.get(0).amount());
        assertEquals("COFFEE BAR", entries.get(1).description());
        assertEquals(Money.of("-12.50", MYR), entries.get(1).amount());
    }

    @Test
    void filtersToTheRequestedDateRange() throws IOException {
        String path = statement("""
                2025-12-31,OLD,-1.00
                2026-01-10,IN RANGE,-2.00
                2027-01-01,FUTURE,-3.00
                """);
        List<BankFeedEntry> entries = client.fetch(path, FROM, TO);
        assertEquals(1, entries.size());
        assertEquals("IN RANGE", entries.get(0).description());
    }

    @Test
    void handlesQuotedDescriptionsContainingCommas() throws IOException {
        String path = statement("2026-01-10,\"KEDAI MAKAN, JALAN AMPANG\",-45.00\n");
        List<BankFeedEntry> entries = client.fetch(path, FROM, TO);
        assertEquals("KEDAI MAKAN, JALAN AMPANG", entries.get(0).description());
    }

    @Test
    void usesProvidedExternalIdAndSynthesisesStableIdsOtherwise() throws IOException {
        String path = statement("""
                2026-01-10,WITH ID,-5.00,BANKTX-1
                2026-01-10,NO ID,-5.00
                2026-01-10,NO ID,-5.00
                """);
        List<BankFeedEntry> entries = client.fetch(path, FROM, TO);
        assertEquals("BANKTX-1", entries.get(0).externalId());
        assertNotEquals(entries.get(1).externalId(), entries.get(2).externalId());
        // Deterministic: re-parsing yields the same ids.
        assertEquals(entries.get(1).externalId(), client.fetch(path, FROM, TO).get(1).externalId());
    }

    @Test
    void skipsMalformedRows() throws IOException {
        String path = statement("""
                not-a-date,BAD DATE,-1.00
                2026-01-10,BAD AMOUNT,abc
                2026-01-10,GOOD,-1.00
                """);
        List<BankFeedEntry> entries = client.fetch(path, FROM, TO);
        assertEquals(1, entries.size());
        assertEquals("GOOD", entries.get(0).description());
    }

    @Test
    void throwsOnUnreadableFile() {
        assertThrows(PersistenceException.class,
                () -> client.fetch(dir.resolve("missing.csv").toString(), FROM, TO));
    }
}
