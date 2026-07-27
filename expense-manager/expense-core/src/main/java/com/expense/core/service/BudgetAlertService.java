package com.expense.core.service;

import com.expense.core.network.AppNotification;
import com.expense.core.network.NotificationPublisher;
import com.expense.core.report.BudgetUtilization;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

/**
 * Decides when a budget deserves the user's attention. Per the architecture,
 * the core owns the <em>when</em> (evaluated against the month's
 * {@link BudgetUtilization}) and hands the <em>how</em> to the injected
 * {@link NotificationPublisher} seam — a platform channel (Android
 * notification, desktop tray) does the actual delivery.
 *
 * <p>At most one notification is published per check: an {@code ALERT} when the
 * category is over budget, a {@code WARNING} when utilisation has reached the
 * warn threshold, nothing otherwise.</p>
 */
public final class BudgetAlertService {

    /** Default utilisation percentage at which an approaching-limit warning fires. */
    public static final double DEFAULT_WARN_THRESHOLD_PCT = 90.0;

    private final MonthlySummaryService summaries;
    private final NotificationPublisher publisher;
    private final double warnThresholdPct;

    public BudgetAlertService(MonthlySummaryService summaries, NotificationPublisher publisher) {
        this(summaries, publisher, DEFAULT_WARN_THRESHOLD_PCT);
    }

    public BudgetAlertService(MonthlySummaryService summaries, NotificationPublisher publisher,
                              double warnThresholdPct) {
        this.summaries = Objects.requireNonNull(summaries, "summaries");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.warnThresholdPct = warnThresholdPct;
    }

    /**
     * Re-evaluates the budget of the category an expense was just added to (or
     * edited in) and publishes at most one notification for it.
     *
     * @param month      the month the expense falls in
     * @param categoryId the expense's category; {@code null} (uncategorised) is a no-op
     * @return the published notification, so callers can also surface it inline
     */
    public Optional<AppNotification> checkAfterExpenseChange(YearMonth month, Long categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }
        return summaries.summarize(month).budgetUtilization().stream()
                .filter(u -> u.categoryId() == categoryId)
                .findFirst()
                .flatMap(this::evaluate)
                .map(n -> {
                    publisher.publish(n);
                    return n;
                });
    }

    private Optional<AppNotification> evaluate(BudgetUtilization u) {
        if (u.overBudget()) {
            return Optional.of(new AppNotification(AppNotification.Severity.ALERT,
                    "Budget exceeded",
                    u.categoryName() + " spending is " + u.spent() + " against a " + u.limit()
                            + " budget this month (" + Math.round(u.utilizationPct()) + "%)."));
        }
        if (u.utilizationPct() >= warnThresholdPct) {
            return Optional.of(new AppNotification(AppNotification.Severity.WARNING,
                    "Approaching budget limit",
                    u.categoryName() + " has used " + Math.round(u.utilizationPct()) + "% of its "
                            + u.limit() + " budget; " + u.remaining() + " remains."));
        }
        return Optional.empty();
    }
}
