package com.expense.core.network;

/**
 * A user-facing alert, platform-neutral so any delivery channel can render it.
 *
 * @param severity how urgently the alert should be presented
 * @param title    short headline (e.g. "Budget exceeded")
 * @param body     one or two sentences of detail
 */
public record AppNotification(Severity severity, String title, String body) {
    /** Presentation urgency; channels may map this to sound/priority. */
    public enum Severity {
        /** Informational, e.g. a completed sync. */
        INFO,
        /** Needs attention soon, e.g. approaching a budget limit. */
        WARNING,
        /** Needs attention now, e.g. a budget exceeded or sync conflict. */
        ALERT
    }
}
