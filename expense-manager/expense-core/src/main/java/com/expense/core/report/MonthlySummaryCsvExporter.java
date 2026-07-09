package com.expense.core.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Exports a {@link MonthlySummary} to a human- and spreadsheet-friendly CSV with
 * a headline section followed by category, payment-method and budget sections.
 */
public final class MonthlySummaryCsvExporter implements ReportExporter<MonthlySummary> {

    @Override
    public void export(MonthlySummary s, OutputStream out) throws IOException {
        Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        String ccy = s.totalIncome().currency().getCurrencyCode();

        w.write(Csv.row("Monthly Summary", s.month().toString()));
        w.write(Csv.row("Currency", ccy));
        w.write("\n");

        w.write(Csv.row("Metric", "Amount"));
        w.write(Csv.row("Total Income", s.totalIncome().amount().toPlainString()));
        w.write(Csv.row("Total Expenses", s.totalExpense().amount().toPlainString()));
        w.write(Csv.row("Net Balance", s.netBalance().amount().toPlainString()));
        w.write(Csv.row("Savings", s.savings().amount().toPlainString()));
        w.write(Csv.row("Outstanding", s.outstanding().amount().toPlainString()));
        w.write("\n");

        w.write(Csv.row("Category", "Amount", "Share %"));
        for (CategoryBreakdownItem c : s.categoryBreakdown()) {
            w.write(Csv.row(c.categoryName(), c.total().amount().toPlainString(),
                    String.valueOf(c.percentage())));
        }
        w.write("\n");

        w.write(Csv.row("Payment Method", "Amount", "Share %"));
        for (PaymentMethodBreakdownItem p : s.paymentBreakdown()) {
            w.write(Csv.row(p.name(), p.total().amount().toPlainString(),
                    String.valueOf(p.percentage())));
        }
        w.write("\n");

        w.write(Csv.row("Budget Category", "Limit", "Spent", "Remaining", "Utilisation %", "Over?"));
        for (BudgetUtilization b : s.budgetUtilization()) {
            w.write(Csv.row(b.categoryName(), b.limit().amount().toPlainString(),
                    b.spent().amount().toPlainString(), b.remaining().amount().toPlainString(),
                    String.valueOf(b.utilizationPct()), String.valueOf(b.overBudget())));
        }
        w.flush();
    }

    @Override
    public String format() {
        return "csv";
    }
}
