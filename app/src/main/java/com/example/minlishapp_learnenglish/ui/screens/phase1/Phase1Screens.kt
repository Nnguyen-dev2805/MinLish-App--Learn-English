package com.example.minlishapp_learnenglish.ui.screens.phase1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.ui.components.MinLishButton
import com.example.minlishapp_learnenglish.ui.components.MinLishCard
import com.example.minlishapp_learnenglish.ui.components.MinLishPasswordField
import com.example.minlishapp_learnenglish.ui.components.MinLishTextField
import com.example.minlishapp_learnenglish.ui.components.MinLishTonalButton
import com.example.minlishapp_learnenglish.ui.components.MinLishTopBar
import com.example.minlishapp_learnenglish.ui.components.StatCard
import com.example.minlishapp_learnenglish.ui.components.TagChip
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun SplashPlaceholderScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg),
            modifier = Modifier.padding(MinLishSpacing.screenMargin)
        ) {
            BrandMark(size = 96.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "English Learning simplified",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer
            )
            MinLishTonalButton(text = "Continue", onClick = onContinue)
        }
    }
}

@Composable
fun OnboardingPlaceholderScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MinLishCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                BrandMark()
                Text(
                    text = "Learn words with rhythm",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "A quiet, focused vocabulary app shaped around flashcards, daily review, and steady progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                MinLishButton(
                    text = "Get Started",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.School
                )
                MinLishTonalButton(
                    text = "Log In",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Outlined.Login
                )
            }
        }
    }
}

@Composable
fun LoginPlaceholderScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MinLishCard(modifier = Modifier.fillMaxWidth(), tonal = false) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
            ) {
                BrandMark(size = 64.dp)
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Welcome back. This is a static Phase 1 login shell.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                MinLishTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                MinLishPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {}) {
                        Text(text = "Forgot Password?")
                    }
                }
                MinLishButton(
                    text = "Log In",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Outlined.Login
                )
                DividerLabel(text = "Or continue with")
                MinLishTonalButton(
                    text = "Continue with Google",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                MinLishTonalButton(
                    text = "Register",
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RegisterPlaceholderScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.Center
    ) {
        MinLishCard(modifier = Modifier.fillMaxWidth(), tonal = false) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Phase 1 keeps this screen static. Auth wiring starts in Phase 3.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MinLishButton(
                    text = "Continue to Home",
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth()
                )
                MinLishTonalButton(
                    text = "Already have an account",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HomePlaceholderScreen(
    onStartLearning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        HomeHeader()
        DailyPlanPreview(onStartLearning = onStartLearning)
        Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)) {
            MiniStatCard(
                title = "Day Streak",
                value = "7",
                icon = Icons.Outlined.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = "Learned",
                value = "450",
                icon = Icons.Outlined.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = "Accuracy",
                value = "88%",
                icon = Icons.Outlined.Insights,
                modifier = Modifier.weight(1f)
            )
        }
        ActivityPreview()
        Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            TipCard(
                title = "Spaced Repetition",
                message = "Reviewing cards often keeps memory fresh.",
                icon = Icons.Outlined.Lightbulb,
                modifier = Modifier.weight(1f)
            )
            TipCard(
                title = "Word Clubs",
                message = "Static placeholder until backend support exists.",
                icon = Icons.Outlined.Groups,
                modifier = Modifier.weight(1f),
                secondary = true
            )
        }
        Spacer(modifier = Modifier.height(MinLishSpacing.lg))
    }
}

@Composable
fun DecksPlaceholderScreen() {
    PlaceholderMainTab(
        title = "Vocabulary Decks",
        subtitle = "Deck list/detail/create screens arrive in Phase 5.",
        chips = listOf("4000 Essential", "IELTS", "Daily")
    )
}

@Composable
fun LearnPlaceholderScreen() {
    PlaceholderMainTab(
        title = "Flashcards",
        subtitle = "Flashcard learning and SRS review submit arrive in Phase 7.",
        chips = listOf("Again", "Hard", "Good", "Easy")
    )
}

@Composable
fun ProgressPlaceholderScreen() {
    PlaceholderMainTab(
        title = "Progress Analytics",
        subtitle = "Dashboard charts and retention cards arrive in Phase 9.",
        chips = listOf("Streak", "Accuracy", "Retention")
    )
}

@Composable
fun ProfilePlaceholderScreen() {
    PlaceholderMainTab(
        title = "Profile & Settings",
        subtitle = "Profile, reminders, and logout wiring arrive in Phase 10.",
        chips = listOf("Goal", "Level", "Reminders")
    )
}

@Composable
private fun BrandMark(
    size: Dp = 72.dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = if (size >= 80.dp) CircleShape else RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

@Composable
private fun DailyPlanPreview(
    onStartLearning: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
        ) {
            Text(text = "Daily Plan", style = MaterialTheme.typography.titleLarge)
            PlanLine(icon = Icons.Outlined.AutoStories, title = "10 New Words", subtitle = "Ready to explore")
            PlanLine(icon = Icons.Outlined.History, title = "25 Due for Review", subtitle = "Keep them fresh")
            MinLishTonalButton(
                text = "Start Learning",
                icon = Icons.Outlined.PlayArrow,
                onClick = onStartLearning,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MinLishSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "A", style = MaterialTheme.typography.labelLarge)
                }
            }
            Column {
                Text(
                    text = "Hello, Alex!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "MinLish",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                modifier = Modifier.padding(MinLishSpacing.xs)
            )
        }
    }
}

@Composable
private fun PlanLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(MinLishSpacing.xs)
            )
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.xxs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (title == "Day Streak") {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivityPreview() {
    MinLishCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Activity", style = MaterialTheme.typography.titleLarge)
                TagChip(text = "This Week")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs),
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.40f, 0.65f, 0.30f, 0.90f, 0.55f, 0.75f, 0.45f).forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp * value)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (index == 3) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primaryFixedDim
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(MinLishSpacing.xs))
                        Text(
                            text = listOf("M", "T", "W", "T", "F", "S", "S")[index],
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TipCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    secondary: Boolean = false
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        color = if (secondary) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryFixed
        },
        contentColor = if (secondary) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onTertiaryFixed
        }
    ) {
        Column(
            modifier = Modifier.padding(MinLishSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (secondary) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryFixedVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun DividerLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PlaceholderMainTab(
    title: String,
    subtitle: String,
    chips: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MinLishSpacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.lg)
    ) {
        MinLishTopBar(title = title)
        MinLishCard(modifier = Modifier.fillMaxWidth(), tonal = false) {
            Column(verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.xs)) {
                    chips.forEach { TagChip(text = it) }
                }
                StatCard(
                    title = "Phase 1 skeleton",
                    value = "Ready",
                    icon = Icons.Outlined.Insights,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
