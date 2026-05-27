package com.example.minlishapp_learnenglish.core.network

import com.example.minlishapp_learnenglish.core.storage.TokenStorage
import com.example.minlishapp_learnenglish.data.remote.api.AuthApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun createPublicRetrofit(
        moshi: Moshi = createMoshi(),
        baseUrl: String = NetworkConfig.API_BASE_URL
    ): Retrofit {
        return createRetrofit(
            okHttpClient = createBaseClient(),
            moshi = moshi,
            baseUrl = baseUrl
        )
    }

    fun createAuthenticatedRetrofit(
        tokenStorage: TokenStorage,
        refreshApi: AuthApi,
        moshi: Moshi = createMoshi(),
        baseUrl: String = NetworkConfig.API_BASE_URL
    ): Retrofit {
        val client = createBaseClient()
            .newBuilder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .authenticator(TokenAuthenticator(tokenStorage, refreshApi))
            .build()

        return createRetrofit(
            okHttpClient = client,
            moshi = moshi,
            baseUrl = baseUrl
        )
    }

    private fun createRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private fun createBaseClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}
