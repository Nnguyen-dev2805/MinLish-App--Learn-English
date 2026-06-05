package com.example.minlishapp_learnenglish.data.remote.api

import com.example.minlishapp_learnenglish.data.remote.dto.ActivityResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.DashboardResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.RetentionResponseDto
import retrofit2.http.GET


// Header -> xác nhận người gửi là ai - token
// BOdy -> POST
interface AnalyticsApi {
    @GET("analytics/dashboard")
    suspend fun getDashboard(): DashboardResponseDto

    @GET("analytics/activity")
    suspend fun getActivity(): ActivityResponseDto

    @GET("analytics/retention")
    suspend fun getRetention(): RetentionResponseDto
}
