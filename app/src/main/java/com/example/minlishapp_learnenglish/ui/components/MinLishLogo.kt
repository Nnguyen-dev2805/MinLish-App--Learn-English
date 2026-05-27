package com.example.minlishapp_learnenglish.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.ui.theme.MinLishSpacing

@Composable
fun MinLishLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    showText: Boolean = true,
    horizontal: Boolean = false
) {
    if (horizontal) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(MinLishSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconSize > 0.dp) {
                LogoMark(iconSize = iconSize)
            }
            if (showText) LogoText(textAlign = TextAlign.Start)
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MinLishSpacing.sm)
        ) {
            if (iconSize > 0.dp) {
                LogoMark(iconSize = iconSize)
            }
            if (showText) LogoText(textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LogoMark(iconSize: Dp) {
    Surface(
        modifier = Modifier.size(iconSize),
        shape = if (iconSize >= 88.dp) CircleShape else RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                modifier = Modifier.size(iconSize * 0.5f)
            )
        }
    }
}

@Composable
private fun LogoText(textAlign: TextAlign) {
    Column(
        horizontalAlignment = if (textAlign == TextAlign.Center) {
            Alignment.CenterHorizontally
        } else {
            Alignment.Start
        }
    ) {
        Text(
            text = "MinLish",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = textAlign
        )
        Text(
            text = "English Learning simplified",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
