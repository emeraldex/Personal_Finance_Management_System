package com.expense.core.network;

import java.time.Instant;

/**
 * An authenticated user session. Deliberately provider-neutral: the token is an
 * opaque string so Firebase ID tokens, OAuth access tokens or custom JWTs all
 * fit without the core depending on any auth library.
 *
 * @param userId    stable id of the authenticated user
 * @param token     opaque bearer token for authenticating outbound calls
 * @param expiresAt when the token stops being valid
 */
public record AuthSession(String userId, String token, Instant expiresAt) {
    /** Returns whether the session is still valid at the given moment. */
    public boolean isActiveAt(Instant now) {
        return now.isBefore(expiresAt);
    }
}
