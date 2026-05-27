package com.example.minlishapp_learnenglish.presentation.viewmodel.auth

import com.example.minlishapp_learnenglish.core.result.AppError
import com.example.minlishapp_learnenglish.core.result.AppResult
import com.example.minlishapp_learnenglish.data.repository.AuthRepository
import com.example.minlishapp_learnenglish.domain.model.AuthSession
import com.example.minlishapp_learnenglish.domain.model.User
import com.example.minlishapp_learnenglish.domain.usecase.auth.LoginUseCase
import com.example.minlishapp_learnenglish.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `login validates empty fields before calling repository`() {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(LoginUseCase(repository))

        viewModel.login()

        assertEquals(0, repository.loginCalls)
        assertNotNull(viewModel.uiState.value.emailError)
        assertNotNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `login success emits navigate home effect`() = runTest {
        val repository = FakeAuthRepository(loginResult = AppResult.Success(sampleSession))
        val viewModel = LoginViewModel(LoginUseCase(repository))
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onEmailChange("student@minlish.app")
        viewModel.onPasswordChange("123456")
        viewModel.login()
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateHome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.apiError)
    }

    @Test
    fun `login failure stores api error and emits snackbar`() = runTest {
        val error = AppError.Unauthorized("Sai email hoặc mật khẩu.", "INVALID_CREDENTIALS")
        val repository = FakeAuthRepository(loginResult = AppResult.Failure(error))
        val viewModel = LoginViewModel(LoginUseCase(repository))
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onEmailChange("student@minlish.app")
        viewModel.onPasswordChange("wrong-password")
        viewModel.login()
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.ShowSnackbar(error.message)), effects)
        assertEquals(error.message, viewModel.uiState.value.apiError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register validates name password and terms before calling repository`() {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(RegisterUseCase(repository))

        viewModel.onEmailChange("student@minlish.app")
        viewModel.onPasswordChange("123")
        viewModel.register()

        assertEquals(0, repository.registerCalls)
        assertNotNull(viewModel.uiState.value.nameError)
        assertNotNull(viewModel.uiState.value.passwordError)
        assertNotNull(viewModel.uiState.value.termsError)
    }

    @Test
    fun `register success emits navigate home effect`() = runTest {
        val repository = FakeAuthRepository(registerResult = AppResult.Success(sampleSession))
        val viewModel = RegisterViewModel(RegisterUseCase(repository))
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }

        viewModel.onNameChange("Min Lish")
        viewModel.onEmailChange("student@minlish.app")
        viewModel.onPasswordChange("123456")
        viewModel.onTermsChange(true)
        viewModel.register()
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateHome), effects)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.apiError)
    }
}

private class FakeAuthRepository(
    private val loginResult: AppResult<AuthSession> = AppResult.Success(sampleSession),
    private val registerResult: AppResult<AuthSession> = AppResult.Success(sampleSession)
) : AuthRepository {
    var loginCalls = 0
        private set
    var registerCalls = 0
        private set

    override suspend fun login(email: String, password: String): AppResult<AuthSession> {
        loginCalls += 1
        return loginResult
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<AuthSession> {
        registerCalls += 1
        return registerResult
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<AuthSession> {
        return AppResult.Failure(AppError.Validation("Google login chưa dùng trong test."))
    }

    override suspend fun refresh(refreshToken: String): AppResult<String> {
        return AppResult.Success("new-access-token")
    }

    override suspend fun logout(refreshToken: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    override suspend fun getMe(): AppResult<User> {
        return AppResult.Success(sampleUser)
    }

    override suspend fun updateMe(
        name: String?,
        goal: String?,
        level: String?,
        dailyNewWords: Int?
    ): AppResult<User> {
        return AppResult.Success(sampleUser.copy(name = name ?: sampleUser.name))
    }
}

private val sampleUser = User(
    id = 1L,
    email = "student@minlish.app",
    name = "Min Lish",
    goal = null,
    level = null,
    dailyNewWords = 10
)

private val sampleSession = AuthSession(
    accessToken = "access-token",
    refreshToken = "refresh-token",
    user = sampleUser
)
