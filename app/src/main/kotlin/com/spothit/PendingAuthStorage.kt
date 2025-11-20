package com.spothit

import android.content.Context
import android.content.SharedPreferences
import com.spothit.core.auth.PkceParameters

class PendingAuthStorage(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun save(pkceParameters: PkceParameters, creationParams: CreationParams) {
        preferences.edit()
            .putString(KEY_CODE_VERIFIER, pkceParameters.codeVerifier)
            .putString(KEY_CODE_CHALLENGE, pkceParameters.codeChallenge)
            .putString(KEY_STATE, pkceParameters.state)
            .putString(KEY_HOST_NAME, creationParams.hostName)
            .putInt(KEY_TOTAL_ROUNDS, creationParams.totalRounds)
            .putString(KEY_AVATAR_URL, creationParams.avatarUrl)
            .apply()
    }

    fun getPkce(): PkceParameters? {
        val codeVerifier = preferences.getString(KEY_CODE_VERIFIER, null)
        val codeChallenge = preferences.getString(KEY_CODE_CHALLENGE, null)
        val state = preferences.getString(KEY_STATE, null)

        if (codeVerifier.isNullOrBlank() || codeChallenge.isNullOrBlank() || state.isNullOrBlank()) {
            return null
        }

        return PkceParameters(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge,
            state = state
        )
    }

    fun getCreationParams(): CreationParams? {
        val hostName = preferences.getString(KEY_HOST_NAME, null)
        val totalRounds = preferences.getInt(KEY_TOTAL_ROUNDS, -1)
        val avatarUrl = preferences.getString(KEY_AVATAR_URL, null)

        if (hostName.isNullOrBlank() || totalRounds <= 0) {
            return null
        }

        return CreationParams(hostName = hostName, totalRounds = totalRounds, avatarUrl = avatarUrl)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREF_NAME = "pending_auth_state"
        const val KEY_CODE_VERIFIER = "code_verifier"
        const val KEY_CODE_CHALLENGE = "code_challenge"
        const val KEY_STATE = "state"
        const val KEY_HOST_NAME = "host_name"
        const val KEY_TOTAL_ROUNDS = "total_rounds"
        const val KEY_AVATAR_URL = "avatar_url"
    }
}
