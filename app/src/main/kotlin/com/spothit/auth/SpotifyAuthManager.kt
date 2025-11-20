package com.spothit.auth

import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private const val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

/**
 * Data holder for authorization tokens and expiration time.
 */
data class SpotifyTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long
) {
    fun isExpired(leewayMillis: Long = 60_000): Boolean {
        return System.currentTimeMillis() + leewayMillis >= expiresAtMillis
    }
}

/**
 * PKCE parameters used during the authorization flow.
 */
data class PkceParameters(
    val codeVerifier: String,
    val codeChallenge: String,
    val state: String
) {
    companion object {
        fun create(random: SecureRandom = SecureRandom()): PkceParameters {
            val codeVerifier = generateCodeVerifier(random)
            val codeChallenge = generateCodeChallenge(codeVerifier)
            val state = generateState(random)
            return PkceParameters(codeVerifier, codeChallenge, state)
        }

        @VisibleForTesting
        fun generateCodeVerifier(random: SecureRandom): String {
            val codeVerifierBytes = ByteArray(64)
            random.nextBytes(codeVerifierBytes)
            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(codeVerifierBytes)
        }

        @VisibleForTesting
        fun generateCodeChallenge(codeVerifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashed = digest.digest(codeVerifier.toByteArray(StandardCharsets.US_ASCII))
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed)
        }

        private fun generateState(random: SecureRandom): String {
            val stateBytes = ByteArray(16)
            random.nextBytes(stateBytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes)
        }
    }
}

/**
 * Result captured when Spotify redirects back to the application.
 */
data class AuthRedirectResult(
    val code: String?,
    val state: String?,
    val error: String?
)

class SpotifyAuthManager(
    private val tokenStorage: TokenStorage,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    private val clientId = "a09e31c757704f4b94153b2ba8845c1b"
    private val clientSecret = "4d328e5daebb418f9a571d50b2295424"
    private val redirectUri: Uri = Uri.parse("spothit://callback")

    fun createAuthorizationIntent(scopes: List<String>, pkceParameters: PkceParameters = PkceParameters.create()): Pair<Intent, PkceParameters> {
        val uri = buildAuthorizationUri(pkceParameters, scopes)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return intent to pkceParameters
    }

    fun buildAuthorizationUri(pkceParameters: PkceParameters, scopes: List<String>): Uri {
        val builder = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirectUri.toString())
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", pkceParameters.codeChallenge)
            .appendQueryParameter("state", pkceParameters.state)

        if (scopes.isNotEmpty()) {
            builder.appendQueryParameter("scope", scopes.joinToString(separator = " "))
        }
        return builder.build()
    }

    fun parseRedirect(uri: Uri): AuthRedirectResult {
        return AuthRedirectResult(
            code = uri.getQueryParameter("code"),
            state = uri.getQueryParameter("state"),
            error = uri.getQueryParameter("error")
        )
    }

    suspend fun exchangeCodeForTokens(authCode: String, pkceParameters: PkceParameters): SpotifyTokens? {
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", authCode)
            .add("redirect_uri", redirectUri.toString())
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("code_verifier", pkceParameters.codeVerifier)
            .build()

        val tokens = performTokenRequest(body)
        if (tokens != null) {
            tokenStorage.saveTokens(tokens)
        }
        return tokens
    }

    suspend fun refreshSession(): SpotifyTokens? {
        val tokens = tokenStorage.getTokens() ?: return null
        val refreshed = refreshTokens(tokens.refreshToken)
        if (refreshed != null) {
            tokenStorage.saveTokens(refreshed)
            return refreshed
        }
        return null
    }

    suspend fun refreshTokens(refreshToken: String): SpotifyTokens? {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        return performTokenRequest(body, refreshToken)
    }

    suspend fun ensureValidAccessToken(): String? {
        val tokens = tokenStorage.getTokens() ?: return null
        if (!tokens.isExpired()) {
            return tokens.accessToken
        }
        return refreshTokens(tokens.refreshToken)?.let { refreshed ->
            tokenStorage.saveTokens(refreshed)
            refreshed.accessToken
        }
    }

    fun clearSession() {
        tokenStorage.clear()
    }

    private suspend fun performTokenRequest(body: RequestBody, fallbackRefreshToken: String? = null): SpotifyTokens? {
        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val responseBody = response.body?.string() ?: return@use null
                parseTokenResponse(responseBody, fallbackRefreshToken)
            }
        }
    }

    @VisibleForTesting
    fun parseTokenResponse(rawBody: String, fallbackRefreshToken: String? = null): SpotifyTokens {
        val json = org.json.JSONObject(rawBody)
        val accessToken = json.getString("access_token")
        val expiresIn = json.optLong("expires_in", 3600L)
        val refreshToken = json.optString("refresh_token", fallbackRefreshToken)
            ?: throw IllegalStateException("Missing refresh token")
        val expiresAt = clock() + expiresIn * 1000L
        return SpotifyTokens(accessToken = accessToken, refreshToken = refreshToken, expiresAtMillis = expiresAt)
    }
}
