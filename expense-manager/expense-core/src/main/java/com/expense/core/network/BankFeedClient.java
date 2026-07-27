package com.expense.core.network;

import java.time.LocalDate;
import java.util.List;

/**
 * Seam for automatic transaction import from a linked bank account. A concrete
 * implementation (an open-banking aggregator, a bank's own API, a statement
 * parser) is injected by an outer module. Entries come back as neutral drafts
 * — not domain entities — because the user must still map them to an account,
 * confirm categories and resolve duplicates before anything is persisted;
 * the manual-entry flow already covers that confirmation step.
 */
public interface BankFeedClient {
    /**
     * Fetches transactions posted to the linked bank account in the given
     * date range (inclusive).
     *
     * @param linkedAccountRef provider-specific reference to the linked bank account
     * @param from             first posting date to include
     * @param to               last posting date to include
     * @return draft entries in posting order, oldest first
     */
    List<BankFeedEntry> fetch(String linkedAccountRef, LocalDate from, LocalDate to);
}
