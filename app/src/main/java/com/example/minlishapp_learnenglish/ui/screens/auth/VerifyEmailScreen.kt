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
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.VerifyEmailUiState
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    uiState: VerifyEmailUiState,
    snackbarHostState: SnackbarHostState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Verify Email", fontWeight = FontWeight.SemiBold) },
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
                        imageVector = Icons.Outlined.MarkEmailRead,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Check your email",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter the 6-digit code sent to ${uiState.email}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.apiError != null) {
                        Text(
                            text = uiState.apiError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    MinLishTextField(
                        value = uiState.otp,
                        onValueChange = onOtpChange,
                        label = "Verification code",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = uiState.otpError,
                        isError = uiState.otpError != null,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MinLishButton(
                        text = "Verify Email",
                        onClick = onVerify,
                        loading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onResend, enabled = !uiState.isLoading) {
                        Text("Resend code")
                    }
                }
            }
        }
    }
}
