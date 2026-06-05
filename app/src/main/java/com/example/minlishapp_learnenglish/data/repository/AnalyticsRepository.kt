package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.core.result.map
import com.example.minlishapp_learnenglish.data.remote.api.AnalyticsApi
import com.example.minlishapp_learnenglish.data.remote.dto.toDomain
import com.example.minlishapp_learnenglish.domain.model.DailyActivity
import com.example.minlishapp_learnenglish.domain.model.ProgressStats
import com.example.minlishapp_learnenglish.domain.model.RetentionStats
import com.squareup.moshi.Moshi

interface AnalyticsRepository {
    suspend fun getDashboard(): AppResult<ProgressStats>
    suspend fun getActivity(): AppResult<List<DailyActivity>>
    suspend fun getRetention(): AppResult<RetentionStats>
}

class DefaultAnalyticsRepository(
    private val analyticsApi: AnalyticsApi,
    private val moshi: Moshi
) : AnalyticsRepository {
    // DTO với Domain Model dùng hiển thị lên UI
    override suspend fun getDashboard(): AppResult<ProgressStats> {
        return safeApiCall(moshi) {
            analyticsApi.getDashboard() // DTO từ backend
        }.map { it.toDomain() } // dto từ backend.toDomain
    }

    override suspend fun getActivity(): AppResult<List<DailyActivity>> {
        return safeApiCall(moshi) {
            analyticsApi.getActivity()
        }.map { response ->
            response.days.map { it.toDomain() }
        }
    }

    override suspend fun getRetention(): AppResult<RetentionStats> {
        return safeApiCall(moshi) {
            analyticsApi.getRetention()
        }.map { it.toDomain() }
    }
}
