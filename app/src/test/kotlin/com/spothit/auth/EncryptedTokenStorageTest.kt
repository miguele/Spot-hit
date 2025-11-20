package com.spothit.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EncryptedTokenStorageTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `tokens are persisted and readable`() {
        val storage = EncryptedTokenStorage(context)
        storage.clear()
        val expectedTokens = SpotifyTokens(
            accessToken = "access",
            refreshToken = "refresh",
            expiresAtMillis = 1234L
        )

        storage.saveTokens(expectedTokens)

        val newInstance = EncryptedTokenStorage(context)
        assertEquals(expectedTokens, newInstance.getTokens())
    }

    @Test
    fun `tokensFlow emits updates on save and clear`() = runTest {
        val storage = EncryptedTokenStorage(context)
        storage.clear()
        val tokens = SpotifyTokens(
            accessToken = "live-access",
            refreshToken = "live-refresh",
            expiresAtMillis = 42L
        )

        val emissions = mutableListOf<SpotifyTokens?>()
        val job = launch { storage.tokensFlow.take(3).toList(emissions) }

        storage.saveTokens(tokens)
        storage.clear()

        job.join()

        assertEquals(listOf(null, tokens, null), emissions)
        assertNull(storage.getTokens())
    }
}
