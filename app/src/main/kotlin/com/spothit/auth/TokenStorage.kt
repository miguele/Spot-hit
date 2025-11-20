package com.spothit.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStorage {
    val tokensFlow: Flow<SpotifyTokens?>
    fun saveTokens(tokens: SpotifyTokens)
    fun getTokens(): SpotifyTokens?
    fun clear()
}

class EncryptedTokenStorage(
    context: Context,
    prefsFactory: EncryptedPrefsFactory = EncryptedPrefsFactory()
) : TokenStorage {

    private val sharedPreferences: SharedPreferences = prefsFactory.create(context, PREF_NAME)
    private val _tokensFlow = MutableStateFlow(readTokens())
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key in TOKEN_KEYS) {
            _tokensFlow.value = readTokens()
        }
    }

    override val tokensFlow: Flow<SpotifyTokens?> = _tokensFlow.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun saveTokens(tokens: SpotifyTokens) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_EXPIRES_AT, tokens.expiresAtMillis)
            .apply()
        _tokensFlow.value = tokens
    }

    override fun getTokens(): SpotifyTokens? = _tokensFlow.value

    override fun clear() {
        sharedPreferences.edit().clear().apply()
        _tokensFlow.value = null
    }

    private fun readTokens(): SpotifyTokens? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, -1)

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || expiresAt <= 0) {
            return null
        }
        return SpotifyTokens(accessToken = accessToken, refreshToken = refreshToken, expiresAtMillis = expiresAt)
    }

    private companion object {
        const val PREF_NAME = "spotify_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
        val TOKEN_KEYS = setOf(KEY_ACCESS_TOKEN, KEY_REFRESH_TOKEN, KEY_EXPIRES_AT)
    }
}
