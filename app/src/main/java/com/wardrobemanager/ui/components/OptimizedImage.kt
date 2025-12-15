package com.wardrobemanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import java.io.File

/**
 * Optimized image component with lazy loading, caching, and memory management
 */
@Composable
fun OptimizedImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(8.dp),
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
    thumbnailSize: Int = 200
) {
    val context = LocalContext.current
    
    // Create optimized image request
    val imageRequest = remember(imagePath, thumbnailSize) {
        ImageRequest.Builder(context)
            .data(File(imagePath))
            .size(Size(thumbnailSize, thumbnailSize))
            .crossfade(300)
            .memoryCacheKey("${imagePath}_${thumbnailSize}")
            .diskCacheKey("${imagePath}_${thumbnailSize}")
            .build()
    }
    
    val painter = rememberAsyncImagePainter(imageRequest)
    val painterState = painter.state
    
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when (painterState) {
            is AsyncImagePainter.State.Loading -> {
                loading?.invoke() ?: DefaultLoadingIndicator()
            }
            is AsyncImagePainter.State.Error -> {
                error?.invoke() ?: DefaultErrorIndicator()
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            else -> {
                placeholder?.invoke() ?: DefaultPlaceholder()
            }
        }
    }
}

/**
 * Thumbnail version with smaller size for grid views
 */
@Composable
fun ThumbnailImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    OptimizedImage(
        imagePath = imagePath,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        shape = shape,
        thumbnailSize = 150 // Smaller size for thumbnails
    )
}

/**
 * Full-size image for detail views
 */
@Composable
fun DetailImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    OptimizedImage(
        imagePath = imagePath,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        shape = shape,
        thumbnailSize = 800 // Larger size for detail views
    )
}

@Composable
private fun DefaultLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun DefaultErrorIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "图片加载失败",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun DefaultPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * Lazy image grid component for efficient loading of multiple images
 */
@Composable
fun LazyImageGrid(
    imagePaths: List<String>,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemSpacing: androidx.compose.ui.unit.Dp = 8.dp
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        androidx.compose.foundation.lazy.grid.items(imagePaths) { imagePath ->
            ThumbnailImage(
                imagePath = imagePath,
                contentDescription = "Image",
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
        }
    }
}