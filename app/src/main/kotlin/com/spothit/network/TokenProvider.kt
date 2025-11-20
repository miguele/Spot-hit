package com.spothit.network

import java.util.concurrent.atomic.AtomicReference

interface TokenProvider {
    fun accessToken(): String?
}

class InMemoryTokenProvider(initialToken: String? = null) : TokenProvider {
    private val tokenRef = AtomicReference(initialToken)

    fun update(token: String?) {
        tokenRef.set(token)
    }

    override fun accessToken(): String? = tokenRef.get()
}
