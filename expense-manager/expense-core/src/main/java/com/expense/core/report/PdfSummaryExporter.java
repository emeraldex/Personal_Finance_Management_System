package com.expense.core.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.io.OutputStream;

/**
 * PDFBox-backed {@link ReportExporter} that renders a {@link MonthlySummary} to a
 * simple, printable PDF: a headline totals block followed by category,
 * payment-method and budget sections. Interchangeable with the CSV and XLSX
 * summary exporters behind {@link ReportExporter}.
 */
public final class PdfSummaryExporter implements ReportExporter<MonthlySummary> {

    private static final float MARGIN = 50f;
    private static final float LEADING = 15f;
    private static final PDType1Font BODY = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public void export(MonthlySummary s, OutputStream out) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            Cursor c = new Cursor(doc);
            String ccy = s.totalIncome().currency().getCurrencyCode();

            c.text("Monthly Summary — " + s.month(), BOLD, 16);
            c.text("Currency: " + ccy, BODY, 11);
            c.gap();

            c.text("Totals", BOLD, 13);
            c.text("Total Income:    " + s.totalIncome().amount().toPlainString(), BODY, 11);
            c.text("Total Expenses:  " + s.totalExpense().amount().toPlainString(), BODY, 11);
            c.text("Net Balance:     " + s.netBalance().amount().toPlainString(), BODY, 11);
            c.text("Savings:         " + s.savings().amount().toPlainString(), BODY, 11);
            c.text("Outstanding:     " + s.outstanding().amount().toPlainString(), BODY, 11);
            c.gap();

            c.text("Spending by Category", BOLD, 13);
            for (CategoryBreakdownItem item : s.categoryBreakdown()) {
                c.text(pad(item.categoryName(), 24) + pad(item.total().amount().toPlainString(), 14)
                        + item.percentage() + "%", BODY, 11);
            }
            c.gap();

            c.text("Spending by Payment Method", BOLD, 13);
            for (PaymentMethodBreakdownItem item : s.paymentBreakdown()) {
                c.text(pad(item.name(), 24) + pad(item.total().amount().toPlainString(), 14)
                        + item.percentage() + "%", BODY, 11);
            }

            if (!s.budgetUtilization().isEmpty()) {
                c.gap();
                c.text("Budgets", BOLD, 13);
                for (BudgetUtilization b : s.budgetUtilization()) {
                    c.text(pad(b.categoryName(), 20)
                            + "limit " + pad(b.limit().amount().toPlainString(), 10)
                            + "spent " + pad(b.spent().amount().toPlainString(), 10)
                            + b.utilizationPct() + "%" + (b.overBudget() ? "  OVER" : ""), BODY, 11);
                }
            }

            c.close();
            doc.save(out);
        }
    }

    @Override
    public String format() {
        return "pdf";
    }

    private static String pad(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) {
            return v.substring(0, width - 1) + " ";
        }
        return v + " ".repeat(width - v.length());
    }

    /**
     * Sanitises text to the WinAnsi range the standard-14 fonts can encode, so an
     * exotic character in a description can never abort the whole export.
     */
    private static String safe(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sb.append(ch >= 32 && ch < 256 ? ch : '?');
        }
        return sb.toString();
    }

    /** Tracks the current page, content stream and vertical position, paging as needed. */
    private static final class Cursor {
        private final PDDocument doc;
        private PDPageContentStream stream;
        private float y;

        Cursor(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        void text(String s, PDType1Font font, float size) throws IOException {
            if (y <= MARGIN) {
                newPage();
            }
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(safe(s));
            stream.endText();
            y -= LEADING;
        }

        void gap() {
            y -= LEADING;
        }

        void close() throws IOException {
            stream.close();
        }
    }
}
