package com.expense.core.network;

/**
 * Seam for future cloud synchronisation and multi-user support. The core depends
 * only on this interface; a concrete HTTP client is provided by an outer module
 * so the business logic stays offline-first and transport-agnostic.
 */
public interface SyncClient {
    /** Pushes local changes upstream and returns the server's response payload. */
    SyncPayload push(SyncPayload localChanges);

    /** Pulls remote changes accumulated since the payload's high-water mark. */
    SyncPayload pull(SyncPayload highWaterMark);
}
