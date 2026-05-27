package com.example.minlishapp_learnenglish.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.LoginUiState
import com.example.minlishapp_learnenglish.ui.components.AuthDividerLabel
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishLogo
import com.example.minlishapp_learnenglish.ui.components.MinLishPasswordField
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.components.SocialLoginButton
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing
import androidx.compose.material3.Scaffold

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MinLishSpacing.screenMargin)
                .padding(top = 48.dp, bottom = MinLishSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            MinLishCard(modifier = Modifier.fillMaxWidth(), tonal = false) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MinLishLogo(iconSize = 56.dp, showText = true)
                    Text(
                        text = "Welcome back! Please login to continue your language journey.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    MinLishTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = "Email address",
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.emailError != null,
                        supportingText = uiState.emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    MinLishPasswordField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = "Password",
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.passwordError != null,
                        supportingText = uiState.passwordError
                    )
                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = MinLishSpacing.xs, vertical = 0.dp)
                    ) {
                        Text(text = "Forgot Password?")
                    }
                    if (uiState.apiError != null) {
                        Text(
                            text = uiState.apiError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MinLishButton(
                        text = "Log In",
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.AutoMirrored.Outlined.Login,
                        loading = uiState.isLoading
                    )
                    AuthDividerLabel(text = "Or continue with")
                    SocialLoginButton(
                        text = "Continue with Google",
                        onClick = onGoogleLogin,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )
                    TextButton(
                        onClick = onRegister,
                        enabled = !uiState.isLoading,
                        contentPadding = PaddingValues(horizontal = MinLishSpacing.xs, vertical = 0.dp)
                    ) {
                        Text(text = "Don't have an account? Register")
                    }
                }
            }
        }
    }
}
