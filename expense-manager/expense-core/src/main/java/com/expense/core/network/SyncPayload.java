package com.expense.core.network;

import java.time.Instant;
import java.util.List;

/**
 * Transport envelope for cloud synchronisation. Deliberately format-neutral so a
 * future REST/gRPC backend can serialise it (e.g. via Jackson) without the core
 * depending on any transport library.
 *
 * @param deviceId    stable id of the originating device
 * @param since       high-water mark of the last successful sync
 * @param changes     opaque, serialised change records
 * @param generatedAt when this payload was assembled
 */
public record SyncPayload(String deviceId, Instant since, List<String> changes, Instant generatedAt) {
    public SyncPayload {
        changes = List.copyOf(changes);
    }
}
