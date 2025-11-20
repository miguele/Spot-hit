package com.spothit.auth

import android.content.Context
import android.content.SharedPreferences
import com.spothit.core.auth.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.jvm.Volatile

interface TokenStorage {
    val tokensFlow: Flow<AuthTokens?>
    fun saveTokens(tokens: AuthTokens)
    fun getTokens(): AuthTokens?
    fun clear()
}

class EncryptedTokenStorage(
    context: Context,
    prefsFactory: EncryptedPrefsFactory = EncryptedPrefsFactory()
) : TokenStorage {

    private val sharedPreferences: SharedPreferences = prefsFactory.create(context, PREF_NAME)
    @Volatile
    private var cachedTokens: AuthTokens? = readTokens()
    private val tokensState = MutableStateFlow(cachedTokens)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key in TOKEN_KEYS) {
            val tokens = readTokens()
            cachedTokens = tokens
            tokensState.value = tokens
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override val tokensFlow: Flow<AuthTokens?> = tokensState

    override fun saveTokens(tokens: AuthTokens) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_EXPIRES_AT, tokens.expiresAtMillis)
            .commit()
        cachedTokens = tokens
        tokensState.value = tokens
    }

    override fun getTokens(): AuthTokens? = cachedTokens

    override fun clear() {
        sharedPreferences.edit().clear().commit()
        cachedTokens = null
        tokensState.value = null
    }

    private fun readTokens(): AuthTokens? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, -1)

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || expiresAt <= 0) {
            return null
        }
        return AuthTokens(accessToken = accessToken, refreshToken = refreshToken, expiresAtMillis = expiresAt)
    }

    private companion object {
        const val PREF_NAME = "spotify_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
        val TOKEN_KEYS = setOf(KEY_ACCESS_TOKEN, KEY_REFRESH_TOKEN, KEY_EXPIRES_AT)
    }
}
