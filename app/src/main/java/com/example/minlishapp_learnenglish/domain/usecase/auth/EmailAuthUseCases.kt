package com.example.minlishapp_learnenglish.domain.usecase.auth

import com.example.minlishapp_learnenglish.data.repository.AuthRepository

class VerifyEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String) = authRepository.verifyEmail(email, otp)
}

class ResendVerificationOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) = authRepository.resendVerificationOtp(email)
}

class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) = authRepository.forgotPassword(email)
}

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String) =
        authRepository.resetPassword(email, otp, newPassword)
}
