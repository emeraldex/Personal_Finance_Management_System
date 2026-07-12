package com.expense.desktop.io;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.service.ExpenseManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Exports a month's transactions to a name-based {@code .xlsx} workbook that the
 * {@link PoiWorkbookImporter} can read back. Names (not database ids) are written
 * so the file is human-editable and portable between databases.
 */
public final class TransactionWorkbookExporter {

    /** The column order shared with {@link PoiWorkbookImporter}. */
    static final String[] HEADERS =
            {"Date", "Type", "Amount", "Category", "Account", "PaymentMethod", "Description"};

    private final ExpenseManager manager;

    public TransactionWorkbookExporter(ExpenseManager manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    public void export(YearMonth month, OutputStream out) throws IOException {
        Map<Long, String> accountNames = new HashMap<>();
        for (Account a : manager.accounts().list()) {
            accountNames.put(a.id(), a.name());
        }
        Map<Long, String> categoryNames = new HashMap<>();
        for (Category c : manager.categories().list()) {
            categoryNames.put(c.id(), c.name());
        }
        Map<Long, String> paymentNames = new HashMap<>();
        for (PaymentMethod p : manager.paymentMethods().list()) {
            paymentNames.put(p.id(), p.name());
        }

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Transactions");
            CellStyle header = headerStyle(wb);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(header);
            }

            int r = 1;
            for (Expense e : manager.expenses().listByMonth(month)) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(e.date().toString());
                row.createCell(1).setCellValue("Expense");
                row.createCell(2).setCellValue(e.signedAmount().abs().amount().doubleValue());
                row.createCell(3).setCellValue(name(categoryNames, e.categoryId()));
                row.createCell(4).setCellValue(name(accountNames, e.accountId()));
                row.createCell(5).setCellValue(name(paymentNames, e.paymentMethodId()));
                row.createCell(6).setCellValue(e.description());
            }
            for (Income i : manager.incomes().listByMonth(month)) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(i.date().toString());
                row.createCell(1).setCellValue("Income");
                row.createCell(2).setCellValue(i.signedAmount().abs().amount().doubleValue());
                row.createCell(3).setCellValue(name(categoryNames, i.categoryId()));
                row.createCell(4).setCellValue(name(accountNames, i.accountId()));
                row.createCell(5).setCellValue("");
                row.createCell(6).setCellValue(i.description());
            }
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
        }
    }

    private static String name(Map<Long, String> names, Long id) {
        return id == null ? "" : names.getOrDefault(id, "");
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
