package com.expense.core.mapper;

import com.expense.core.domain.*;
import com.expense.core.util.Money;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;

/**
 * Central collection of {@link ResultSet}-to-domain mapping functions. Keeping
 * them together avoids scattering brittle column-name literals and guarantees a
 * single definition of how each row shape is decoded.
 */
public final class Mappers {

    private Mappers() {
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    public static Category category(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getString("name"),
                CategoryType.valueOf(rs.getString("type")),
                rs.getString("color_hex"),
                rs.getString("icon"),
                rs.getInt("archived") == 1);
    }

    public static PaymentMethod paymentMethod(ResultSet rs) throws SQLException {
        return new PaymentMethod(
                rs.getLong("id"),
                rs.getString("name"),
                PaymentMethodType.valueOf(rs.getString("type")),
                rs.getInt("archived") == 1);
    }

    public static Account account(ResultSet rs) throws SQLException {
        Currency ccy = Currency.getInstance(rs.getString("currency"));
        return new Account(
                rs.getLong("id"),
                rs.getString("name"),
                AccountType.valueOf(rs.getString("type")),
                Money.ofMinor(rs.getLong("opening_balance_minor"), ccy),
                rs.getInt("archived") == 1);
    }

    public static Expense expense(ResultSet rs) throws SQLException {
        Currency ccy = Currency.getInstance(rs.getString("currency"));
        return new Expense(
                rs.getLong("id"),
                rs.getLong("account_id"),
                nullableLong(rs, "category_id"),
                nullableLong(rs, "payment_method_id"),
                Money.ofMinor(rs.getLong("amount_minor"), ccy),
                rs.getString("description"),
                LocalDate.parse(rs.getString("txn_date")),
                Instant.parse(rs.getString("created_at")));
    }

    public static Income income(ResultSet rs) throws SQLException {
        Currency ccy = Currency.getInstance(rs.getString("currency"));
        return new Income(
                rs.getLong("id"),
                rs.getLong("account_id"),
                nullableLong(rs, "category_id"),
                Money.ofMinor(rs.getLong("amount_minor"), ccy),
                rs.getString("description"),
                LocalDate.parse(rs.getString("txn_date")),
                Instant.parse(rs.getString("created_at")));
    }

    public static Budget budget(ResultSet rs) throws SQLException {
        Currency ccy = Currency.getInstance(rs.getString("currency"));
        return new Budget(
                rs.getLong("id"),
                rs.getLong("category_id"),
                YearMonth.parse(rs.getString("month")),
                Money.ofMinor(rs.getLong("limit_minor"), ccy));
    }

    public static MonthlySummarySnapshot summary(ResultSet rs) throws SQLException {
        Currency ccy = Currency.getInstance(rs.getString("currency"));
        return new MonthlySummarySnapshot(
                rs.getLong("id"),
                YearMonth.parse(rs.getString("month")),
                Money.ofMinor(rs.getLong("total_income_minor"), ccy),
                Money.ofMinor(rs.getLong("total_expense_minor"), ccy),
                Money.ofMinor(rs.getLong("net_minor"), ccy),
                Money.ofMinor(rs.getLong("savings_minor"), ccy),
                Money.ofMinor(rs.getLong("outstanding_minor"), ccy),
                Instant.parse(rs.getString("generated_at")));
    }
}
