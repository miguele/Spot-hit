package com.spothit.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class SessionProvider(
    private val authManager: SpotifyAuthManager,
    private val tokenStorage: TokenStorage
) {
    suspend fun getAccessToken(): String? = authManager.ensureValidAccessToken()

    suspend fun createAuthenticatedRequest(original: Request): Request? {
        val token = withContext(Dispatchers.IO) { getAccessToken() } ?: return null
        return original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    fun clearSession() {
        tokenStorage.clear()
    }
}
