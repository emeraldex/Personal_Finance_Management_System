package com.expense.desktop.io;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.report.ImportResult;
import com.expense.core.report.WorkbookImporter;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * POI-backed {@link WorkbookImporter}. Reads a transaction workbook whose first
 * sheet has a header row (Date, Type, Amount, Category, Account, PaymentMethod,
 * Description) and imports each row through the same validated services used for
 * manual entry. Accounts, categories and payment methods are matched by name and
 * created on demand when absent, so a legacy workbook imports cleanly against a
 * fresh database. Blank and invalid rows are skipped and reported.
 */
public final class PoiWorkbookImporter implements WorkbookImporter {

    private final ExpenseManager manager;
    private final Currency currency;

    public PoiWorkbookImporter(ExpenseManager manager, Currency currency) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = Objects.requireNonNull(currency);
    }

    @Override
    public ImportResult importWorkbook(InputStream in) throws IOException {
        int imported = 0;
        int skipped = 0;
        List<String> warnings = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getNumberOfSheets() == 0 ? null : wb.getSheetAt(0);
            if (sheet == null) {
                return new ImportResult(0, 0, List.of("Workbook has no sheets"));
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return new ImportResult(0, 0, List.of("Workbook has no header row"));
            }
            Columns cols = Columns.from(headerRow);
            if (cols.date < 0 || cols.amount < 0) {
                return new ImportResult(0, 0,
                        List.of("Missing required columns: a 'Date' and 'Amount' header are required"));
            }

            for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String dateText = str(row, cols.date);
                String amountText = numericOrString(row, cols.amount);
                if (dateText.isBlank() && amountText.isBlank()) {
                    continue; // fully blank row — not counted
                }
                try {
                    importRow(row, cols, warnings);
                    imported++;
                } catch (RowException e) {
                    skipped++;
                    warnings.add("Row " + (r + 1) + ": " + e.getMessage());
                } catch (ExpenseException e) {
                    skipped++;
                    warnings.add("Row " + (r + 1) + ": " + e.getMessage());
                }
            }
        }
        return new ImportResult(imported, skipped, warnings);
    }

    private void importRow(Row row, Columns cols, List<String> warnings) {
        LocalDate date = parseDate(row, cols.date);
        BigDecimal amount = parseAmount(row, cols.amount);
        String type = cols.type < 0 ? "expense" : str(row, cols.type).toLowerCase(Locale.ROOT);
        boolean income = type.startsWith("inc");

        String accountName = cols.account < 0 ? "" : str(row, cols.account);
        if (accountName.isBlank()) {
            throw new RowException("account name is required");
        }
        long accountId = resolveAccount(accountName, warnings);

        String categoryName = cols.category < 0 ? "" : str(row, cols.category);
        Long categoryId = categoryName.isBlank() ? null
                : resolveCategory(categoryName, income ? CategoryType.INCOME : CategoryType.EXPENSE, warnings);

        String description = cols.description < 0 ? "" : str(row, cols.description);
        Money money = Money.of(amount, currency);

        if (income) {
            manager.incomes().create(new CreateIncomeRequest(accountId, categoryId, money, description, date));
        } else {
            String paymentName = cols.payment < 0 ? "" : str(row, cols.payment);
            Long paymentId = paymentName.isBlank() ? null : resolvePayment(paymentName, warnings);
            manager.expenses().create(
                    new CreateExpenseRequest(accountId, categoryId, paymentId, money, description, date));
        }
    }

    // --- resolve-or-create helpers --------------------------------------

    private long resolveAccount(String name, List<String> warnings) {
        for (Account a : manager.accounts().list()) {
            if (a.name().equalsIgnoreCase(name.strip())) {
                return a.id();
            }
        }
        Account created = manager.accounts().create(
                new CreateAccountRequest(name.strip(), AccountType.OTHER, Money.zero(currency)));
        warnings.add("Created account '" + created.name() + "'");
        return created.id();
    }

    private long resolveCategory(String name, CategoryType type, List<String> warnings) {
        for (Category c : manager.categories().listByType(type)) {
            if (c.name().equalsIgnoreCase(name.strip())) {
                return c.id();
            }
        }
        Category created = manager.categories().create(
                new CreateCategoryRequest(name.strip(), type, null, null));
        warnings.add("Created " + type + " category '" + created.name() + "'");
        return created.id();
    }

    private long resolvePayment(String name, List<String> warnings) {
        for (PaymentMethod p : manager.paymentMethods().list()) {
            if (p.name().equalsIgnoreCase(name.strip())) {
                return p.id();
            }
        }
        PaymentMethod created = manager.paymentMethods().create(
                new CreatePaymentMethodRequest(name.strip(), PaymentMethodType.OTHER));
        warnings.add("Created payment method '" + created.name() + "'");
        return created.id();
    }

    // --- cell parsing ----------------------------------------------------

    private LocalDate parseDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            throw new RowException("missing date");
        }
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = str(row, col);
        try {
            return LocalDate.parse(text.strip());
        } catch (Exception e) {
            throw new RowException("unparseable date '" + text + "' (use YYYY-MM-DD)");
        }
    }

    private BigDecimal parseAmount(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            throw new RowException("missing amount");
        }
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue()).abs();
            }
            return new BigDecimal(str(row, col).strip()).abs();
        } catch (NumberFormatException e) {
            throw new RowException("unparseable amount");
        }
    }

    private static String str(Row row, int col) {
        if (col < 0) {
            return "";
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().strip();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static String numericOrString(Row row, int col) {
        return str(row, col);
    }

    /** Header-name to column-index resolution, tolerant of order and spacing. */
    private static final class Columns {
        int date = -1;
        int type = -1;
        int amount = -1;
        int category = -1;
        int account = -1;
        int payment = -1;
        int description = -1;

        static Columns from(Row header) {
            Columns c = new Columns();
            for (Cell cell : header) {
                if (cell.getCellType() != org.apache.poi.ss.usermodel.CellType.STRING) {
                    continue;
                }
                String key = cell.getStringCellValue().toLowerCase(Locale.ROOT).replace(" ", "");
                int i = cell.getColumnIndex();
                switch (key) {
                    case "date" -> c.date = i;
                    case "type" -> c.type = i;
                    case "amount" -> c.amount = i;
                    case "category" -> c.category = i;
                    case "account" -> c.account = i;
                    case "paymentmethod", "payment" -> c.payment = i;
                    case "description", "notes", "note" -> c.description = i;
                    default -> { }
                }
            }
            return c;
        }
    }

    /** Signals a row that cannot be imported; carried as a warning. */
    private static final class RowException extends RuntimeException {
        RowException(String message) {
            super(message);
        }
    }
}
