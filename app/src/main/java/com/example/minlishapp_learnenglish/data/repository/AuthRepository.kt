package com.example.minlishapp_learnenglish.data.repository

import com.example.minlishapp_learnenglish.core.email.EmailSender
import com.example.minlishapp_learnenglish.core.otp.OtpManager
import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.local.dao.UserDao
import com.example.minlishapp_learnenglish.data.local.entity.UserEntity
import com.example.minlishapp_learnenglish.data.local.mapper.toDomain
import com.example.minlishapp_learnenglish.domain.model.User
import kotlinx.coroutines.CancellationException

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        goal: String? = null,
        level: String? = null
    ): AppResult<User>
    suspend fun logout(refreshToken: String = ""): AppResult<Unit>
    suspend fun getMe(): AppResult<User>
    suspend fun updateMe(
        name: String? = null,
        goal: String? = null,
        level: String? = null,
        dailyNewWords: Int? = null
    ): AppResult<User>
    suspend fun sendForgotPasswordOtp(email: String): AppResult<Unit>
    suspend fun verifyOtp(email: String, otp: String): AppResult<Unit>
    suspend fun verifyOtpAndResetPassword(
        email: String,
        otp: String,
        newPassword: String
    ): AppResult<Unit>
}

class DefaultAuthRepository(
    private val userDao: UserDao,
    private val otpManager: OtpManager
) : AuthRepository {
    override suspend fun login(email: String, password: String): AppResult<User> {
        return localCall {
            val cleanEmail = email.trim().lowercase()
            require(cleanEmail.isNotBlank()) { "Email is required." }
            require(password.isNotBlank()) { "Password is required." }

            val user = userDao.getUserByEmail(cleanEmail)
                ?: throw LocalAuthException("Invalid email or password.")
            if (user.password != password) {
                throw LocalAuthException("Invalid email or password.")
            }

            userDao.logoutAll()
            userDao.markLoggedIn(user.id)
            val loggedInUser = userDao.getUserById(user.id)
                ?: throw LocalAuthException("User not found.")
            loggedInUser.toDomain()
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        goal: String?,
        level: String?
    ): AppResult<User> {
        return localCall {
            val cleanName = name.trim()
            val cleanEmail = email.trim().lowercase()
            require(cleanName.isNotBlank()) { "Full name is required." }
            require(cleanEmail.isNotBlank()) { "Email is required." }
            require(password.length >= 6) { "Password must be at least 6 characters." }

            if (userDao.getUserByEmail(cleanEmail) != null) {
                throw LocalAuthException("This email is already registered.")
            }

            userDao.logoutAll()
            // Demo-only: password is stored locally in plain text to keep the course project simple.
            val userId = userDao.insertUser(
                UserEntity(
                    email = cleanEmail,
                    password = password,
                    name = cleanName,
                    goal = goal,
                    level = level,
                    dailyNewWords = 10,
                    isLoggedIn = true
                )
            )
            val user = userDao.getUserById(userId)
                ?: throw LocalAuthException("Created user not found.")
            user.toDomain()
        }
    }

    override suspend fun logout(refreshToken: String): AppResult<Unit> {
        return localCall {
            userDao.logoutAll()
        }
    }


    override suspend fun getMe(): AppResult<User> {
        return localCall {
            val user = userDao.getLoggedInUser()
                ?: throw LocalAuthException("Please log in first.")
            user.toDomain()
        }
    }

    override suspend fun updateMe(
        name: String?,
        goal: String?,
        level: String?,
        dailyNewWords: Int?
    ): AppResult<User> {
        return localCall {
            val currentUser = userDao.getLoggedInUser()
                ?: throw LocalAuthException("Please log in first.")
            val updatedUser = currentUser.copy(
                name = name?.trim()?.takeIf { it.isNotBlank() } ?: currentUser.name,
                goal = goal?.trim()?.takeIf { it.isNotBlank() } ?: currentUser.goal,
                level = level?.trim()?.takeIf { it.isNotBlank() } ?: currentUser.level,
                dailyNewWords = dailyNewWords ?: currentUser.dailyNewWords
            )
            userDao.updateUser(updatedUser)
            updatedUser.toDomain()
        }
    }

    override suspend fun sendForgotPasswordOtp(email: String): AppResult<Unit> {
        return localCall {
            val cleanEmail = email.trim().lowercase()
            require(cleanEmail.isNotBlank()) { "Email is required." }
            userDao.getUserByEmail(cleanEmail)
                ?: throw LocalAuthException("Email not found.")

            val otp = (100000..999999).random().toString()
            otpManager.saveOtp(cleanEmail, otp)

            val sent = EmailSender.sendOtpEmail(cleanEmail, otp)
            if (!sent) {
                throw LocalAuthException("Failed to send email. Check your internet.")
            }
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): AppResult<Unit> {
        return localCall {
            val cleanEmail = email.trim().lowercase()
            val savedEmail = otpManager.getEmail()
            if (savedEmail != cleanEmail) {
                throw LocalAuthException("Invalid OTP.")
            }
            if (!otpManager.isOtpValid(otp.trim())) {
                throw LocalAuthException("Invalid or expired OTP.")
            }
            otpManager.markVerified()
        }
    }

    override suspend fun verifyOtpAndResetPassword(
        email: String,
        otp: String,
        newPassword: String
    ): AppResult<Unit> {
        return localCall {
            val cleanEmail = email.trim().lowercase()
            require(newPassword.length >= 6) { "Password must be at least 6 characters." }

            if (otpManager.getEmail() != cleanEmail) {
                throw LocalAuthException("Session expired. Start again.")
            }
            if (!otpManager.isVerified()) {
                if (!otpManager.isOtpValid(otp.trim())) {
                    throw LocalAuthException("Invalid or expired OTP.")
                }
                otpManager.markVerified()
            }

            userDao.getUserByEmail(cleanEmail)
                ?: throw LocalAuthException("Email not found.")
            userDao.updatePasswordByEmail(cleanEmail, newPassword)
            otpManager.clear()
        }
    }

    private suspend fun <T> localCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: LocalAuthException) {
            AppResult.Failure(AppError.Validation(message = error.message ?: "Authentication failed."))
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(message = error.message ?: "Invalid input."))
        } catch (error: Exception) {
            AppResult.Failure(
                AppError.Unknown(
                    message = error.message ?: "Local database error."
                )
            )
        }
    }

    private class LocalAuthException(message: String) : Exception(message)
}
