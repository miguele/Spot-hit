package com.spothit.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitProvider {
    private fun defaultMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun moshiConverterFactory(moshi: Moshi = defaultMoshi()): Converter.Factory {
        return MoshiConverterFactory.create(moshi)
    }

    fun create(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory = moshiConverterFactory()
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }
}
