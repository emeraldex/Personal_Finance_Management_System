package com.expense.core.report;

import com.expense.core.util.Money;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void exportsMonthlySummaryAsCsv() throws Exception {
        MonthlySummary summary = new MonthlySummary(
                YearMonth.of(2026, 1),
                Money.of("3000.00", USD), Money.of("-1700.00", USD),
                Money.of("1300.00", USD), Money.of("1300.00", USD), Money.zero(USD),
                List.of(new CategoryBreakdownItem(1L, "Rent", Money.of("-1000.00", USD), 58.82)),
                List.of(new PaymentMethodBreakdownItem(1L, "Cash", Money.of("-1700.00", USD), 100.0)),
                List.of(),
                List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MonthlySummaryCsvExporter().export(summary, out);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("Total Income,3000.00"));
        assertTrue(csv.contains("Net Balance,1300.00"));
        assertTrue(csv.contains("Rent,-1000.00,58.82"));
    }

    @Test
    void escapesFieldsContainingCommas() throws Exception {
        MonthlySummary summary = new MonthlySummary(
                YearMonth.of(2026, 1),
                Money.zero(USD), Money.zero(USD), Money.zero(USD), Money.zero(USD), Money.zero(USD),
                List.of(new CategoryBreakdownItem(1L, "Food, Drink", Money.of("-10.00", USD), 100.0)),
                List.of(), List.of(), List.of());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MonthlySummaryCsvExporter().export(summary, out);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("\"Food, Drink\""));
    }
}
