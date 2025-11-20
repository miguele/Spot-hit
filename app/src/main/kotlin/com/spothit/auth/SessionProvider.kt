package com.spothit.auth

import com.spothit.core.auth.AuthSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class SessionProvider(
    private val authManager: AuthSessionManager,
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
