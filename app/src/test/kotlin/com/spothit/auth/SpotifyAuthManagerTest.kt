package com.spothit.auth

import com.spothit.core.auth.AuthTokens
import com.spothit.core.auth.PkceParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.SecureRandom

class SpotifyAuthManagerTest {

    @Test
    fun `pkce challenge matches RFC example`() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

        val generatedChallenge = SpotifyAuthManager.generateCodeChallenge(verifier)

        assertEquals(expectedChallenge, generatedChallenge)
    }

    @Test
    fun `generated verifier is within allowed length`() {
        val params = PkceParameters.create(SecureRandom())

        assertTrue(params.codeVerifier.length in 43..128)
        assertEquals(
            SpotifyAuthManager.generateCodeChallenge(params.codeVerifier),
            params.codeChallenge
        )
    }

    @Test
    fun `token parser uses fallback refresh token when missing`() {
        val clock = { 1_000L }
        val storage = InMemoryTokenStorage()
        val manager = SpotifyAuthManager(storage, clock = clock)
        val rawBody = """
            {"access_token":"abc123","expires_in":3600}
        """.trimIndent()

        val parsed = manager.parseTokenResponse(rawBody, fallbackRefreshToken = "refresh-me")

        assertEquals("abc123", parsed.accessToken)
        assertEquals("refresh-me", parsed.refreshToken)
        assertTrue(parsed.expiresAtMillis > clock())
    }

    @Test
    fun `token parser stores provided refresh token`() {
        val storage = InMemoryTokenStorage()
        val manager = SpotifyAuthManager(storage)
        val rawBody = """
            {"access_token":"abc123","refresh_token":"provided","expires_in":1200}
        """.trimIndent()

        val parsed = manager.parseTokenResponse(rawBody)
        assertNotNull(parsed)
        assertEquals("provided", parsed.refreshToken)
    }

    private class InMemoryTokenStorage : TokenStorage {
        private var tokens: AuthTokens? = null
        private val tokensState = MutableStateFlow<AuthTokens?>(null)

        override val tokensFlow: Flow<AuthTokens?>
            get() = tokensState

        override fun saveTokens(tokens: AuthTokens) {
            this.tokens = tokens
            tokensState.value = tokens
        }

        override fun getTokens(): AuthTokens? = tokensState.value

        override fun clear() {
            tokens = null
            tokensState.value = null
        }
    }
}
