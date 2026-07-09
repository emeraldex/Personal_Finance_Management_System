package com.expense.core.network;

import com.expense.core.dto.CreateExpenseRequest;

/**
 * Seam for OCR-based receipt scanning. An implementation turns raw image bytes
 * into a draft expense the user can confirm. Kept in the core as an interface so
 * platform-specific OCR engines (ML Kit on Android, a cloud OCR on desktop) can
 * be injected without altering business logic.
 */
public interface ReceiptScanner {
    /**
     * Extracts a draft expense from receipt image bytes.
     *
     * @param imageBytes the raw image (JPEG/PNG)
     * @param accountId  the account to attribute the draft to
     * @return a populated draft request for user confirmation
     */
    CreateExpenseRequest scan(byte[] imageBytes, long accountId);
}
