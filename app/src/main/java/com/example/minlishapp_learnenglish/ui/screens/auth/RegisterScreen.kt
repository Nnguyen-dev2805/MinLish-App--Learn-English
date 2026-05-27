package com.example.minlishapp_learnenglish.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.auth.RegisterUiState
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCheckboxRow
import com.example.minlishapp_learnenglish.ui.components.MinLishDropdown
import com.example.minlishapp_learnenglish.ui.components.MinLishPasswordField
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
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
        ) {
            RegisterHero()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MinLishSpacing.screenMargin),
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Fill in the details to start your journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MinLishTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Full Name",
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError
                )
                MinLishTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Email Address",
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
                MinLishDropdown(
                    value = uiState.goal,
                    options = listOf("General English", "IELTS Academic", "TOEIC Exam", "Business Communication"),
                    onValueChange = onGoalChange,
                    label = "Learning Goal",
                    modifier = Modifier.fillMaxWidth()
                )
                MinLishDropdown(
                    value = uiState.level,
                    options = listOf("A1 Beginner", "A2 Elementary", "B1 Intermediate", "B2 Upper Intermediate", "C1 Advanced", "C2 Proficiency"),
                    onValueChange = onLevelChange,
                    label = "Current Level",
                    modifier = Modifier.fillMaxWidth()
                )
                MinLishCheckboxRow(
                    checked = uiState.acceptedTerms,
                    onCheckedChange = onTermsChange,
                    text = "I agree to the Terms of Service and Privacy Policy.",
                    error = uiState.termsError
                )
                if (uiState.apiError != null) {
                    Text(
                        text = uiState.apiError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                MinLishButton(
                    text = "Create Account",
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    loading = uiState.isLoading
                )
                TextButton(
                    onClick = onLogin,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Already have an account? Log In")
                }
            }
        }
    }
}

@Composable
private fun RegisterHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier.padding(
                start = MinLishSpacing.screenMargin,
                end = MinLishSpacing.screenMargin,
                top = MinLishSpacing.xl,
                bottom = MinLishSpacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryFixed,
                    modifier = Modifier.padding(end = 2.dp)
                )
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primaryFixed
                )
            }
            Text(
                text = "Master English with quiet efficiency.",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Create your account and start building a daily vocabulary habit.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primaryFixed
            )
        }
    }
}
