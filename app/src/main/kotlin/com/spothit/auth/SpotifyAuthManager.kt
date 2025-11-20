package com.spothit.auth

import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import android.util.Base64
import com.spothit.core.auth.AuthRedirectResult
import com.spothit.core.auth.AuthSessionManager
import com.spothit.core.auth.AuthTokens
import com.spothit.core.auth.AuthorizationRequest
import com.spothit.core.auth.PkceParameters
import com.spothit.auth.SpotifyAuthManager.Companion.create
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

private const val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

class SpotifyAuthManager(
    private val tokenStorage: TokenStorage,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) : AuthSessionManager {

    private val clientId = "a09e31c757704f4b94153b2ba8845c1b"
    private val clientSecret = "4d328e5daebb418f9a571d50b2295424"
    private val redirectUri: Uri = Uri.parse("spothit://callback")

    override fun createAuthorizationIntent(scopes: List<String>): AuthorizationRequest {
        val pkceParameters = PkceParameters.create()
        val uri = buildAuthorizationUri(pkceParameters, scopes)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return AuthorizationRequest(intent = intent, pkceParameters = pkceParameters)
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

    override fun parseRedirect(uri: Uri): AuthRedirectResult {
        return AuthRedirectResult(
            code = uri.getQueryParameter("code"),
            state = uri.getQueryParameter("state"),
            error = uri.getQueryParameter("error")
        )
    }

    override suspend fun exchangeCodeForTokens(authCode: String, parameters: PkceParameters): AuthTokens? {
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", authCode)
            .add("redirect_uri", redirectUri.toString())
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("code_verifier", parameters.codeVerifier)
            .build()

        val tokens = performTokenRequest(body)
        if (tokens != null) {
            tokenStorage.saveTokens(tokens)
        }
        return tokens
    }

    override suspend fun refreshSession(): AuthTokens? {
        val tokens = tokenStorage.getTokens() ?: return null
        val refreshed = refreshTokens(tokens.refreshToken)
        if (refreshed != null) {
            tokenStorage.saveTokens(refreshed)
            return refreshed
        }
        return null
    }

    suspend fun refreshTokens(refreshToken: String): AuthTokens? {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        return performTokenRequest(body, refreshToken)
    }

    override suspend fun ensureValidAccessToken(): String? {
        val tokens = tokenStorage.getTokens() ?: return null
        if (!tokens.isExpired()) {
            return tokens.accessToken
        }
        return refreshTokens(tokens.refreshToken)?.let { refreshed ->
            tokenStorage.saveTokens(refreshed)
            refreshed.accessToken
        }
    }

    override fun clearSession() {
        tokenStorage.clear()
    }

    private suspend fun performTokenRequest(body: RequestBody, fallbackRefreshToken: String? = null): AuthTokens? {
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
    fun parseTokenResponse(rawBody: String, fallbackRefreshToken: String? = null): AuthTokens {
        val json = org.json.JSONObject(rawBody)
        val accessToken = json.getString("access_token")
        val expiresIn = json.optLong("expires_in", 3600L)
        val refreshToken = json
            .optString("refresh_token", fallbackRefreshToken ?: "")
            .ifBlank { fallbackRefreshToken.orEmpty() }
            .ifBlank { throw IllegalStateException("Missing refresh token") }
        val expiresAt = clock() + expiresIn * 1000L
        return AuthTokens(accessToken = accessToken, refreshToken = refreshToken, expiresAtMillis = expiresAt)
    }

    companion object {
        fun PkceParameters.Companion.create(random: SecureRandom = SecureRandom()): PkceParameters {
            val codeVerifier = generateCodeVerifier(random)
            val codeChallenge = generateCodeChallenge(codeVerifier)
            val state = generateState(random)
            return PkceParameters(codeVerifier, codeChallenge, state)
        }

        @VisibleForTesting
        fun generateCodeVerifier(random: SecureRandom): String {
            val codeVerifierBytes = ByteArray(64)
            random.nextBytes(codeVerifierBytes)
            return codeVerifierBytes.encodeUrlSafeNoPadding()
        }

        @VisibleForTesting
        fun generateCodeChallenge(codeVerifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashed = digest.digest(codeVerifier.toByteArray(StandardCharsets.US_ASCII))
            return hashed.encodeUrlSafeNoPadding()
        }

        private fun generateState(random: SecureRandom): String {
            val stateBytes = ByteArray(16)
            random.nextBytes(stateBytes)
            return stateBytes.encodeUrlSafeNoPadding()
        }

        private fun ByteArray.encodeUrlSafeNoPadding(): String {
            return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        }
    }
}
