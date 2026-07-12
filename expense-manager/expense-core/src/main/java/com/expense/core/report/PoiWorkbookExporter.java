package com.expense.core.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * POI-backed {@link WorkbookExporter}: writes a {@link MonthlySummary} to a
 * multi-sheet {@code .xlsx} workbook (Summary, Categories, Payment Methods,
 * Budgets). Plugs in wherever a {@code ReportExporter<MonthlySummary>} is
 * expected — the CSV and PDF exporters are interchangeable with it.
 */
public final class PoiWorkbookExporter implements WorkbookExporter {

    @Override
    public void export(MonthlySummary s, OutputStream out) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = headerStyle(wb);
            String ccy = s.totalIncome().currency().getCurrencyCode();

            Sheet summary = wb.createSheet("Summary");
            int r = 0;
            keyValue(summary, r++, "Monthly Summary", s.month().toString());
            keyValue(summary, r++, "Currency", ccy);
            r++;
            headerRow(summary, r++, header, "Metric", "Amount");
            amountRow(summary, r++, "Total Income", s.totalIncome().amount().doubleValue());
            amountRow(summary, r++, "Total Expenses", s.totalExpense().amount().doubleValue());
            amountRow(summary, r++, "Net Balance", s.netBalance().amount().doubleValue());
            amountRow(summary, r++, "Savings", s.savings().amount().doubleValue());
            amountRow(summary, r, "Outstanding", s.outstanding().amount().doubleValue());
            autoSize(summary, 2);

            Sheet cats = wb.createSheet("Categories");
            r = 0;
            headerRow(cats, r++, header, "Category", "Amount", "Share %");
            for (CategoryBreakdownItem c : s.categoryBreakdown()) {
                Row row = cats.createRow(r++);
                row.createCell(0).setCellValue(c.categoryName());
                row.createCell(1).setCellValue(c.total().amount().doubleValue());
                row.createCell(2).setCellValue(c.percentage());
            }
            autoSize(cats, 3);

            Sheet pms = wb.createSheet("Payment Methods");
            r = 0;
            headerRow(pms, r++, header, "Payment Method", "Amount", "Share %");
            for (PaymentMethodBreakdownItem p : s.paymentBreakdown()) {
                Row row = pms.createRow(r++);
                row.createCell(0).setCellValue(p.name());
                row.createCell(1).setCellValue(p.total().amount().doubleValue());
                row.createCell(2).setCellValue(p.percentage());
            }
            autoSize(pms, 3);

            Sheet budgets = wb.createSheet("Budgets");
            r = 0;
            headerRow(budgets, r++, header, "Category", "Limit", "Spent", "Remaining",
                    "Utilisation %", "Over?");
            for (BudgetUtilization b : s.budgetUtilization()) {
                Row row = budgets.createRow(r++);
                row.createCell(0).setCellValue(b.categoryName());
                row.createCell(1).setCellValue(b.limit().amount().doubleValue());
                row.createCell(2).setCellValue(b.spent().amount().doubleValue());
                row.createCell(3).setCellValue(b.remaining().amount().doubleValue());
                row.createCell(4).setCellValue(b.utilizationPct());
                row.createCell(5).setCellValue(b.overBudget() ? "OVER" : "");
            }
            autoSize(budgets, 6);

            wb.write(out);
        }
    }

    @Override
    public String format() {
        return "xlsx";
    }

    private static void keyValue(Sheet sheet, int r, String key, String value) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
    }

    private static void amountRow(Sheet sheet, int r, String label, double amount) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(amount);
    }

    private static void headerRow(Sheet sheet, int r, CellStyle style, String... titles) {
        Row row = sheet.createRow(r);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(style);
        }
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
