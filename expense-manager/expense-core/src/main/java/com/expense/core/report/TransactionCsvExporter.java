package com.expense.core.report;

import com.expense.core.domain.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exports a flat list of {@link Transaction}s (expenses and/or income) to CSV,
 * one row per transaction with the signed amount preserved.
 */
public final class TransactionCsvExporter implements ReportExporter<List<Transaction>> {

    @Override
    public void export(List<Transaction> data, OutputStream out) throws IOException {
        Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        w.write(Csv.row("Id", "Date", "Type", "Amount", "Currency", "AccountId",
                "CategoryId", "PaymentMethodId", "Description"));
        for (Transaction t : data) {
            w.write(Csv.row(
                    t.id() == null ? "" : String.valueOf(t.id()),
                    t.date().toString(),
                    t.type().name(),
                    t.signedAmount().amount().toPlainString(),
                    t.signedAmount().currency().getCurrencyCode(),
                    String.valueOf(t.accountId()),
                    t.categoryId() == null ? "" : String.valueOf(t.categoryId()),
                    paymentMethodId(t),
                    t.description()));
        }
        w.flush();
    }

    /**
     * Payment method only applies to expenses; income rows leave the column blank.
     */
    private static String paymentMethodId(Transaction t) {
        if (t instanceof com.expense.core.domain.Expense e && e.paymentMethodId() != null) {
            return String.valueOf(e.paymentMethodId());
        }
        return "";
    }

    @Override
    public String format() {
        return "csv";
    }
}
