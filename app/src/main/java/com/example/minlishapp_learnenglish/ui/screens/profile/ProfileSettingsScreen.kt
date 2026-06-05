package com.example.minlishapp_learnenglish.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileUiState
import com.example.minlishapp_learnenglish.presentation.viewmodel.profile.ProfileViewModel
import com.example.minlishapp_learnenglish.ui.components.ErrorStateView
import com.example.minlishapp_learnenglish.ui.components.LoadingStateView
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishDropdown
import com.example.minlishapp_learnenglish.ui.components.MinLishOutlinedButton
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.components.MinLishTonalButton
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun ProfileSettingsScreen(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onDailyNewWordsChange: (String) -> Unit,
    onDailyTimeChange: (String) -> Unit,
    onEmailEnabledChange: (Boolean) -> Unit,
    onPushEnabledChange: (Boolean) -> Unit,
    onSaveProfile: () -> Unit,
    onSaveNotifications: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading && !uiState.hasProfile -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingStateView(message = "Loading profile...")
            }
        }
        uiState.errorMessage != null && !uiState.hasProfile -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(MinLishSpacing.screenMargin),
                contentAlignment = Alignment.Center
            ) {
                ErrorStateView(
                    title = "Unable to load profile",
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
            }
        }
        else -> {
            ProfileSettingsContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onNameChange = onNameChange,
                onGoalChange = onGoalChange,
                onLevelChange = onLevelChange,
                onDailyNewWordsChange = onDailyNewWordsChange,
                onDailyTimeChange = onDailyTimeChange,
                onEmailEnabledChange = onEmailEnabledChange,
                onPushEnabledChange = onPushEnabledChange,
                onSaveProfile = onSaveProfile,
                onSaveNotifications = onSaveNotifications,
                onLogout = onLogout,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProfileSettingsContent(
    uiState: ProfileUiState,
    onRefresh: () -> Unit,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onDailyNewWordsChange: (String) -> Unit,
    onDailyTimeChange: (String) -> Unit,
    onEmailEnabledChange: (Boolean) -> Unit,
    onPushEnabledChange: (Boolean) -> Unit,
    onSaveProfile: () -> Unit,
    onSaveNotifications: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        ProfileTopBar(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )
        ProfileHero(uiState = uiState)
        ProfileFormCard(
            uiState = uiState,
            onNameChange = onNameChange,
            onGoalChange = onGoalChange,
            onLevelChange = onLevelChange,
            onDailyNewWordsChange = onDailyNewWordsChange,
            onSaveProfile = onSaveProfile
        )
        NotificationSettingsCard(
            uiState = uiState,
            onDailyTimeChange = onDailyTimeChange,
            onEmailEnabledChange = onEmailEnabledChange,
            onPushEnabledChange = onPushEnabledChange,
            onSaveNotifications = onSaveNotifications
        )
        StreakCard()
        AccountCard(
            isLoggingOut = uiState.isLoggingOut,
            onLogout = onLogout
        )
        Spacer(modifier = Modifier.height(MinLishSpacing.lg))
    }
}

@Composable
private fun ProfileTopBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Column {
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}

@Composable
private fun ProfileHero(uiState: ProfileUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryFixed,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.name.initials(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Text(
                text = uiState.name.ifBlank { "MinLish Learner" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                HeroChip(text = uiState.goal)
                HeroChip(text = uiState.level)
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfileFormCard(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onDailyNewWordsChange: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    SettingsSectionCard(
        title = "Account",
        icon = Icons.Outlined.Person
    ) {
        MinLishTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = "Full name",
            modifier = Modifier.fillMaxWidth(),
            supportingText = uiState.nameError,
            isError = uiState.nameError != null
        )
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MinLishDropdown(
            value = uiState.goal,
            options = ProfileViewModel.goalOptions,
            onValueChange = onGoalChange,
            label = "Learning goal",
            modifier = Modifier.fillMaxWidth()
        )
        MinLishDropdown(
            value = uiState.level,
            options = ProfileViewModel.levelOptions,
            onValueChange = onLevelChange,
            label = "Current level",
            modifier = Modifier.fillMaxWidth()
        )
        MinLishTextField(
            value = uiState.dailyNewWordsInput,
            onValueChange = onDailyNewWordsChange,
            label = "Daily new words",
            modifier = Modifier.fillMaxWidth(),
            supportingText = uiState.dailyNewWordsError,
            isError = uiState.dailyNewWordsError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        MinLishButton(
            text = "Save profile",
            icon = Icons.Outlined.Save,
            onClick = onSaveProfile,
            loading = uiState.isSavingProfile,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NotificationSettingsCard(
    uiState: ProfileUiState,
    onDailyTimeChange: (String) -> Unit,
    onEmailEnabledChange: (Boolean) -> Unit,
    onPushEnabledChange: (Boolean) -> Unit,
    onSaveNotifications: () -> Unit
) {
    SettingsSectionCard(
        title = "Daily Reminders",
        icon = Icons.Outlined.Notifications
    ) {
        ToggleSettingRow(
            icon = Icons.Outlined.Alarm,
            title = "Daily study reminder",
            subtitle = "Remind me when it is time to study each day.",
            checked = uiState.pushEnabled,
            onCheckedChange = onPushEnabledChange
        )
        ToggleSettingRow(
            icon = Icons.Outlined.Email,
            title = "Email reminder",
            subtitle = "Receive email reminders for your learning schedule.",
            checked = uiState.emailEnabled,
            onCheckedChange = onEmailEnabledChange
        )
        MinLishTextField(
            value = uiState.dailyTime,
            onValueChange = onDailyTimeChange,
            label = "Reminder time",
            modifier = Modifier.fillMaxWidth(),
            supportingText = uiState.dailyTimeError ?: "Use HH:mm format, for example 20:00",
            isError = uiState.dailyTimeError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(
            text = "Timezone: ${uiState.timezone}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MinLishButton(
            text = "Save reminders",
            icon = Icons.Outlined.Save,
            onClick = onSaveNotifications,
            loading = uiState.isSavingNotifications,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StreakCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryFixed,
        contentColor = MaterialTheme.colorScheme.onTertiaryFixed
    ) {
        Row(
            modifier = Modifier.padding(MinLishSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryFixedDim,
                contentColor = MaterialTheme.colorScheme.onTertiaryFixed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Outlined.LocalFireDepartment, contentDescription = null)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily rhythm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Keep daily reminders on so your dashboard and streak stay active.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    isLoggingOut: Boolean,
    onLogout: () -> Unit
) {
    MinLishOutlinedButton(
        text = if (isLoggingOut) "Logging out..." else "Log out",
        icon = Icons.AutoMirrored.Outlined.Logout,
        onClick = onLogout,
        enabled = !isLoggingOut,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    MinLishCard(tonal = false, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
private fun ToggleSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingRowShell(icon = icon, title = title, subtitle = subtitle) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingRowShell(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(MinLishSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryFixed,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing()
        }
    }
}

private fun String.initials(): String {
    val parts = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "ML"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
