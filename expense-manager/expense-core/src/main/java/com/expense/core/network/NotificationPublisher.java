package com.expense.core.network;

/**
 * Seam for surfacing alerts to the user outside the app's own screens —
 * budget-overrun warnings, bill reminders, sync failures. Delivery is a
 * platform concern (Android notification channel, desktop system tray, email),
 * so the core only decides <em>when</em> an alert is warranted and hands the
 * <em>how</em> to an injected implementation. A no-op implementation keeps
 * alerts entirely optional.
 */
public interface NotificationPublisher {
    /**
     * Delivers a notification to the user. Implementations should be
     * fire-and-forget: failures to deliver must not disturb business logic.
     *
     * @param notification what to show
     */
    void publish(AppNotification notification);
}
