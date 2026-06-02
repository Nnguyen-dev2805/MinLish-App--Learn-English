package com.example.minlishapp_learnenglish.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun LoadingStateView(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MinLishSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    MessageStateView(
        title = title,
        message = message,
        icon = { Icon(Icons.Outlined.Inbox, contentDescription = null) },
        modifier = modifier
    )
}

@Composable
fun ErrorStateView(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageStateView(
        title = title,
        message = message,
        icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null) },
        modifier = modifier
    ) {
        MinLishTonalButton(
            text = "Try again",
            icon = Icons.Outlined.Refresh,
            onClick = onRetry
        )
    }
}

@Composable
private fun MessageStateView(
    title: String,
    message: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    MinLishCard(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            icon()
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}
