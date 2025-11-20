package com.spothit.core.auth

import android.content.Intent
import android.net.Uri

/**
 * Domain-level contract for managing an authenticated session.
 *
 * Implementations live in the data layer so that authentication
 * providers can be swapped or mocked without impacting callers.
 */
interface AuthSessionManager {
    fun createAuthorizationIntent(scopes: List<String>): AuthorizationRequest
    fun parseRedirect(uri: Uri): AuthRedirectResult
    suspend fun exchangeCodeForTokens(authCode: String, parameters: PkceParameters): AuthTokens?
    suspend fun refreshSession(): AuthTokens?
    suspend fun ensureValidAccessToken(): String?
    fun clearSession()
}

/**
 * Wrapper around the authorization intent and PKCE parameters used to
 * launch a login flow from the presentation layer.
 */
data class AuthorizationRequest(val intent: Intent, val pkceParameters: PkceParameters)

data class AuthRedirectResult(
    val code: String?,
    val state: String?,
    val error: String?,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
) {
    fun isExpired(leewayMillis: Long = 60_000): Boolean {
        return System.currentTimeMillis() + leewayMillis >= expiresAtMillis
    }
}

data class PkceParameters(
    val codeVerifier: String,
    val codeChallenge: String,
    val state: String,
) {
    companion object
}
