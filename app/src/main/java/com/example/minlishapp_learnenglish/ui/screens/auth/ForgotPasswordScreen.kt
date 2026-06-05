package com.example.minlishapp_learnenglish.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.ForgotPasswordUiState
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishPasswordField
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onResetPassword: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(MinLishSpacing.screenMargin),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MinLishCard(modifier = Modifier.fillMaxWidth(), tonal = false) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (uiState.codeSent) "Reset your password" else "Send reset code",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.codeSent) {
                            "Enter the code from your email and choose a new password."
                        } else {
                            "Enter your account email. We will send a reset code if it exists."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MinLishTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        leadingIcon = { Icon(Icons.Outlined.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        supportingText = uiState.emailError,
                        isError = uiState.emailError != null,
                        enabled = !uiState.codeSent && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.codeSent) {
                        MinLishTextField(
                            value = uiState.otp,
                            onValueChange = onOtpChange,
                            label = "Reset code",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = uiState.otpError,
                            isError = uiState.otpError != null,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MinLishPasswordField(
                            value = uiState.newPassword,
                            onValueChange = onNewPasswordChange,
                            label = "New password",
                            supportingText = uiState.passwordError,
                            isError = uiState.passwordError != null,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MinLishPasswordField(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = "Confirm password",
                            supportingText = uiState.confirmPasswordError,
                            isError = uiState.confirmPasswordError != null,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    MinLishButton(
                        text = if (uiState.codeSent) "Reset Password" else "Send Code",
                        onClick = if (uiState.codeSent) onResetPassword else onSendCode,
                        loading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
