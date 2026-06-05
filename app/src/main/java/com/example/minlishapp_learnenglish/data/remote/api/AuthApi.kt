package com.example.minlishapp_learnenglish.data.remote.api

import com.example.minlishapp_learnenglish.data.remote.dto.AuthResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.ForgotPasswordRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.GoogleLoginRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.LoginRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.LogoutRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.MessageResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.RefreshRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.RefreshResponseDto
import com.example.minlishapp_learnenglish.data.remote.dto.RegisterRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.ResendVerificationOtpRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.ResetPasswordRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.UpdateUserRequestDto
import com.example.minlishapp_learnenglish.data.remote.dto.UserDto
import com.example.minlishapp_learnenglish.data.remote.dto.VerifyEmailRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequestDto): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): RefreshResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto)

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): AuthResponseDto

    @POST("auth/resend-verification-otp")
    suspend fun resendVerificationOtp(@Body request: ResendVerificationOtpRequestDto): MessageResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): MessageResponseDto

    @GET("users/me")
    suspend fun getMe(): UserDto

    // PATCH dùng để cập nhật dữ liệu 1 phần không cập nhật hết
    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateUserRequestDto): UserDto
}
