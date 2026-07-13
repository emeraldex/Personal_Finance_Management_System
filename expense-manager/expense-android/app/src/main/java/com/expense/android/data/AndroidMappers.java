package com.expense.android.data;

import android.database.Cursor;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Budget;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.MonthlySummarySnapshot;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.util.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;

/**
 * {@link Cursor}-to-domain mapping, mirroring the core's JDBC {@code Mappers} so
 * the exact same row shapes decode identically on Android. Columns are resolved
 * by name, so {@code SELECT *} ordering is irrelevant.
 */
final class AndroidMappers {

    private AndroidMappers() {
    }

    static Category category(Cursor c) {
        return new Category(
                l(c, "id"), s(c, "name"), CategoryType.valueOf(s(c, "type")),
                s(c, "color_hex"), s(c, "icon"), i(c, "archived") == 1);
    }

    static PaymentMethod paymentMethod(Cursor c) {
        return new PaymentMethod(
                l(c, "id"), s(c, "name"), PaymentMethodType.valueOf(s(c, "type")),
                i(c, "archived") == 1);
    }

    static Account account(Cursor c) {
        Currency ccy = Currency.getInstance(s(c, "currency"));
        return new Account(
                l(c, "id"), s(c, "name"), AccountType.valueOf(s(c, "type")),
                Money.ofMinor(l(c, "opening_balance_minor"), ccy), i(c, "archived") == 1);
    }

    static Expense expense(Cursor c) {
        Currency ccy = Currency.getInstance(s(c, "currency"));
        return new Expense(
                l(c, "id"), l(c, "account_id"), nullableLong(c, "category_id"),
                nullableLong(c, "payment_method_id"), Money.ofMinor(l(c, "amount_minor"), ccy),
                s(c, "description"), LocalDate.parse(s(c, "txn_date")), Instant.parse(s(c, "created_at")));
    }

    static Income income(Cursor c) {
        Currency ccy = Currency.getInstance(s(c, "currency"));
        return new Income(
                l(c, "id"), l(c, "account_id"), nullableLong(c, "category_id"),
                Money.ofMinor(l(c, "amount_minor"), ccy), s(c, "description"),
                LocalDate.parse(s(c, "txn_date")), Instant.parse(s(c, "created_at")));
    }

    static Budget budget(Cursor c) {
        Currency ccy = Currency.getInstance(s(c, "currency"));
        return new Budget(
                l(c, "id"), l(c, "category_id"), YearMonth.parse(s(c, "month")),
                Money.ofMinor(l(c, "limit_minor"), ccy));
    }

    static MonthlySummarySnapshot summary(Cursor c) {
        Currency ccy = Currency.getInstance(s(c, "currency"));
        return new MonthlySummarySnapshot(
                l(c, "id"), YearMonth.parse(s(c, "month")),
                Money.ofMinor(l(c, "total_income_minor"), ccy),
                Money.ofMinor(l(c, "total_expense_minor"), ccy),
                Money.ofMinor(l(c, "net_minor"), ccy),
                Money.ofMinor(l(c, "savings_minor"), ccy),
                Money.ofMinor(l(c, "outstanding_minor"), ccy),
                Instant.parse(s(c, "generated_at")));
    }

    // --- column helpers --------------------------------------------------

    private static long l(Cursor c, String col) {
        return c.getLong(c.getColumnIndexOrThrow(col));
    }

    private static int i(Cursor c, String col) {
        return c.getInt(c.getColumnIndexOrThrow(col));
    }

    private static String s(Cursor c, String col) {
        return c.getString(c.getColumnIndexOrThrow(col));
    }

    private static Long nullableLong(Cursor c, String col) {
        int idx = c.getColumnIndexOrThrow(col);
        return c.isNull(idx) ? null : c.getLong(idx);
    }
}
