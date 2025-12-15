package com.wardrobemanager.ui.imagepicker

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preloads images for better performance
 */
@Singleton
class ImagePreloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
    private val imageLoadingManager: ImageLoadingManager
) {
    
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Preload images for a list of clothing items
     */
    fun preloadClothingImages(imagePaths: List<String>) {
        preloadScope.launch {
            imagePaths.chunked(5).forEach { chunk ->
                // Process in small chunks to avoid overwhelming the system
                chunk.map { imagePath ->
                    async {
                        preloadSingleImage(imagePath)
                    }
                }.awaitAll()
                
                // Small delay between chunks
                delay(100)
            }
        }
    }
    
    /**
     * Preload a single image
     */
    private suspend fun preloadSingleImage(imagePath: String) {
        try {
            // Preload thumbnail in memory cache
            imageLoadingManager.loadThumbnail(imagePath)
            
            // Also preload in Coil cache
            val request = ImageRequest.Builder(context)
                .data(File(imagePath))
                .size(200, 200)
                .memoryCacheKey("${imagePath}_200")
                .build()
            
            imageLoader.execute(request)
        } catch (e: Exception) {
            // Ignore preload failures
        }
    }
    
    /**
     * Preload images that are likely to be viewed next
     */
    fun preloadUpcomingImages(
        currentIndex: Int,
        allImagePaths: List<String>,
        preloadCount: Int = 3
    ) {
        preloadScope.launch {
            val startIndex = maxOf(0, currentIndex - preloadCount)
            val endIndex = minOf(allImagePaths.size, currentIndex + preloadCount + 1)
            
            val imagesToPreload = allImagePaths.subList(startIndex, endIndex)
            preloadClothingImages(imagesToPreload)
        }
    }
    
    /**
     * Cancel all preloading operations
     */
    fun cancelPreloading() {
        preloadScope.coroutineContext.cancelChildren()
    }
    
    /**
     * Warm up the image loading system
     */
    suspend fun warmUpImageLoading() = withContext(Dispatchers.IO) {
        try {
            // Create a small test image to warm up the system
            imageLoadingManager.getCacheInfo()
        } catch (e: Exception) {
            // Ignore warmup failures
        }
    }
}