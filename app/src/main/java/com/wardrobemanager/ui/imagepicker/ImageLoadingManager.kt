package com.wardrobemanager.ui.imagepicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages optimized image loading with caching and memory management
 */
@Singleton
class ImageLoadingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryManager: MemoryManager
) {
    
    // Memory cache for thumbnails (smaller images)
    private val thumbnailCache: LruCache<String, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // Use 1/8th of available memory
        
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
            
            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }
    
    // Disk cache for processed images
    private val diskCacheDir: File by lazy {
        File(context.cacheDir, "image_cache").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Mutex for thread-safe cache operations
    private val cacheMutex = Mutex()
    
    // Weak references to track active image requests
    private val activeRequests = mutableMapOf<String, WeakReference<Any>>()
    
    /**
     * Load thumbnail with caching and lazy loading
     */
    suspend fun loadThumbnail(
        imagePath: String,
        targetWidth: Int = 200,
        targetHeight: Int = 200
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${imagePath}_${targetWidth}x${targetHeight}"
        
        // Check memory cache first
        thumbnailCache.get(cacheKey)?.let { cachedBitmap ->
            if (!cachedBitmap.isRecycled) {
                return@withContext cachedBitmap
            } else {
                thumbnailCache.remove(cacheKey)
            }
        }
        
        // Check disk cache
        val diskCacheFile = File(diskCacheDir, "${cacheKey.hashCode()}.jpg")
        if (diskCacheFile.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(diskCacheFile.absolutePath)
                if (bitmap != null && !bitmap.isRecycled) {
                    cacheMutex.withLock {
                        thumbnailCache.put(cacheKey, bitmap)
                    }
                    return@withContext bitmap
                }
            } catch (e: Exception) {
                // If disk cache is corrupted, delete the file
                diskCacheFile.delete()
            }
        }
        
        // Load and process original image
        loadAndProcessImage(imagePath, targetWidth, targetHeight, cacheKey, diskCacheFile)
    }
    
    private suspend fun loadAndProcessImage(
        imagePath: String,
        targetWidth: Int,
        targetHeight: Int,
        cacheKey: String,
        diskCacheFile: File
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(imagePath)
            if (!originalFile.exists()) return@withContext null
            
            // Decode with efficient sampling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeFile(imagePath, options)
            
            // Get optimal dimensions based on memory availability
            val (optimalWidth, optimalHeight) = memoryManager.getOptimalImageDimensions(
                options.outWidth, options.outHeight, targetWidth, targetHeight
            )
            
            // Calculate sample size for memory efficiency
            options.inSampleSize = calculateInSampleSize(options, optimalWidth, optimalHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = memoryManager.getOptimalBitmapConfig()
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
                ?: return@withContext null
            
            // Check memory before scaling
            val requiredMemory = memoryManager.calculateBitmapMemory(targetWidth, targetHeight)
            if (!memoryManager.hasEnoughMemory(requiredMemory)) {
                memoryManager.performGarbageCollectionIfNeeded()
            }
            
            // Scale to exact target size if needed
            val scaledBitmap = if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()
                
                val (finalWidth, finalHeight) = if (aspectRatio > targetAspectRatio) {
                    targetWidth to (targetWidth / aspectRatio).toInt()
                } else {
                    (targetHeight * aspectRatio).toInt() to targetHeight
                }
                
                val scaled = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                }
                scaled
            } else {
                bitmap
            }
            
            // Save to disk cache
            try {
                diskCacheFile.outputStream().use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            } catch (e: Exception) {
                // Disk cache save failed, continue without it
            }
            
            // Add to memory cache
            cacheMutex.withLock {
                thumbnailCache.put(cacheKey, scaledBitmap)
            }
            
            scaledBitmap
            
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Preload images for better performance
     */
    suspend fun preloadImages(imagePaths: List<String>) = withContext(Dispatchers.IO) {
        imagePaths.forEach { imagePath ->
            try {
                loadThumbnail(imagePath)
            } catch (e: Exception) {
                // Continue with other images if one fails
            }
        }
    }
    
    /**
     * Clear memory cache to free up memory
     */
    fun clearMemoryCache() {
        thumbnailCache.evictAll()
    }
    
    /**
     * Clear disk cache
     */
    suspend fun clearDiskCache() = withContext(Dispatchers.IO) {
        try {
            diskCacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }
    
    /**
     * Get cache size information
     */
    fun getCacheInfo(): CacheInfo {
        val memorySize = thumbnailCache.size()
        val memoryMaxSize = thumbnailCache.maxSize()
        val diskSize = diskCacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        
        return CacheInfo(
            memoryCacheSize = memorySize,
            memoryCacheMaxSize = memoryMaxSize,
            diskCacheSize = diskSize,
            diskCacheFileCount = diskCacheDir.listFiles()?.size ?: 0
        )
    }
    
    /**
     * Clean up old cache files
     */
    suspend fun cleanupOldCache(maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            diskCacheDir.listFiles()?.forEach { file ->
                if (currentTime - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    data class CacheInfo(
        val memoryCacheSize: Int,
        val memoryCacheMaxSize: Int,
        val diskCacheSize: Long,
        val diskCacheFileCount: Int
    )
}