package com.example.minlishapp_learnenglish.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.minlishapp_learnenglish.core.network.BackendUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun RemoteMediaImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val resolvedUrl = remember(imageUrl) { BackendUrlResolver.resolve(imageUrl) }
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, resolvedUrl) {
        value = withContext(Dispatchers.IO) {
            resolvedUrl?.let { loadImageBitmap(it) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (val loadedBitmap = bitmap) {
            null -> {
                if (resolvedUrl == null) {
                    Icon(
                        imageVector = Icons.Outlined.Photo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Image(
                    bitmap = loadedBitmap,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp)
                )
            }
        }
    }
}

private fun loadImageBitmap(url: String): androidx.compose.ui.graphics.ImageBitmap? {
    return runCatching {
        URL(url).openStream().use { input ->
            BitmapFactory.decodeStream(input)?.asImageBitmap()
        }
    }.getOrNull()
}
