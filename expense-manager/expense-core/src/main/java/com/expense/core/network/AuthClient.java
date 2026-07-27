package com.expense.core.network;

import java.util.Optional;

/**
 * Seam for user authentication, the foundation of multi-user support. The core
 * never talks to an identity provider directly; a concrete implementation
 * (Firebase Auth, OIDC, a custom backend) is injected by an outer module so the
 * business logic stays offline-first and provider-agnostic. Multi-user cloud
 * sync composes this with {@link SyncClient}: the session's user id scopes
 * which data a device pushes and pulls.
 */
public interface AuthClient {
    /**
     * Authenticates the given credentials against the identity provider.
     *
     * @param username the account identifier (e.g. an email address)
     * @param secret   the proof of identity; callers should zero the array
     *                 after the call returns
     * @return the established session
     */
    AuthSession signIn(String username, char[] secret);

    /** Ends the current session and discards any cached tokens. */
    void signOut();

    /** Returns the active session, or empty when signed out or expired. */
    Optional<AuthSession> currentSession();
}
