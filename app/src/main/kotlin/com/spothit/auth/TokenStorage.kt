package com.spothit.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface TokenStorage {
    fun saveTokens(tokens: SpotifyTokens)
    fun getTokens(): SpotifyTokens?
    fun clear()
}

class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "spotify_tokens",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveTokens(tokens: SpotifyTokens) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_EXPIRES_AT, tokens.expiresAtMillis)
            .apply()
    }

    override fun getTokens(): SpotifyTokens? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, -1)

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || expiresAt <= 0) {
            return null
        }
        return SpotifyTokens(accessToken = accessToken, refreshToken = refreshToken, expiresAtMillis = expiresAt)
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
