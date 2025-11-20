package com.spothit.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.spothit.core.auth.AuthTokens
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
        val expectedTokens = AuthTokens(
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
        val tokens = AuthTokens(
            accessToken = "live-access",
            refreshToken = "live-refresh",
            expiresAtMillis = 42L
        )

        val emissions = mutableListOf<AuthTokens?>()
        val job = launch { storage.tokensFlow.take(3).toList(emissions) }

        storage.saveTokens(tokens)
        storage.clear()

        job.join()

        assertEquals(listOf(null, tokens, null), emissions)
        assertNull(storage.getTokens())
    }
}
