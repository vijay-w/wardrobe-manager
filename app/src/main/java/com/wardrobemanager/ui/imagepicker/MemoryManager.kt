package com.wardrobemanager.ui.imagepicker

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages memory usage for image operations
 */
@Singleton
class MemoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    /**
     * Get available memory information
     */
    fun getMemoryInfo(): MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return MemoryInfo(
            availableMemory = memoryInfo.availMem,
            totalMemory = memoryInfo.totalMem,
            isLowMemory = memoryInfo.lowMemory,
            maxHeapSize = maxMemory,
            currentHeapSize = totalMemory,
            usedHeapSize = usedMemory,
            freeHeapSize = freeMemory
        )
    }
    
    /**
     * Check if there's enough memory for an operation
     */
    fun hasEnoughMemory(requiredBytes: Long): Boolean {
        val memoryInfo = getMemoryInfo()
        val availableHeap = memoryInfo.maxHeapSize - memoryInfo.usedHeapSize
        return availableHeap > requiredBytes * 2 // Keep some buffer
    }
    
    /**
     * Calculate memory required for a bitmap
     */
    fun calculateBitmapMemory(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Long {
        val bytesPerPixel = when (config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 4
        }
        return (width * height * bytesPerPixel).toLong()
    }
    
    /**
     * Get optimal bitmap config based on available memory
     */
    fun getOptimalBitmapConfig(): Bitmap.Config {
        val memoryInfo = getMemoryInfo()
        return if (memoryInfo.isLowMemory || memoryInfo.usedHeapSize > memoryInfo.maxHeapSize * 0.8) {
            Bitmap.Config.RGB_565 // Use less memory
        } else {
            Bitmap.Config.ARGB_8888 // Better quality
        }
    }
    
    /**
     * Get optimal image dimensions based on available memory
     */
    fun getOptimalImageDimensions(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Pair<Int, Int> {
        val memoryInfo = getMemoryInfo()
        val availableMemory = memoryInfo.maxHeapSize - memoryInfo.usedHeapSize
        
        // If low on memory, reduce target dimensions
        val memoryFactor = if (memoryInfo.isLowMemory || availableMemory < memoryInfo.maxHeapSize * 0.3) {
            0.5f
        } else if (availableMemory < memoryInfo.maxHeapSize * 0.5) {
            0.75f
        } else {
            1.0f
        }
        
        val adjustedTargetWidth = (targetWidth * memoryFactor).toInt()
        val adjustedTargetHeight = (targetHeight * memoryFactor).toInt()
        
        // Calculate dimensions maintaining aspect ratio
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return if (originalWidth > originalHeight) {
            val width = minOf(adjustedTargetWidth, originalWidth)
            val height = (width / aspectRatio).toInt()
            Pair(width, height)
        } else {
            val height = minOf(adjustedTargetHeight, originalHeight)
            val width = (height * aspectRatio).toInt()
            Pair(width, height)
        }
    }
    
    /**
     * Force garbage collection if memory is low
     */
    suspend fun performGarbageCollectionIfNeeded() = withContext(Dispatchers.IO) {
        val memoryInfo = getMemoryInfo()
        if (memoryInfo.isLowMemory || memoryInfo.usedHeapSize > memoryInfo.maxHeapSize * 0.8) {
            System.gc()
            // Give GC some time to work
            Thread.sleep(100)
        }
    }
    
    /**
     * Check if device is low-end based on memory
     */
    fun isLowEndDevice(): Boolean {
        val memoryInfo = getMemoryInfo()
        val totalMemoryMB = memoryInfo.totalMemory / (1024 * 1024)
        return totalMemoryMB < 2048 // Less than 2GB RAM
    }
    
    data class MemoryInfo(
        val availableMemory: Long,
        val totalMemory: Long,
        val isLowMemory: Boolean,
        val maxHeapSize: Long,
        val currentHeapSize: Long,
        val usedHeapSize: Long,
        val freeHeapSize: Long
    ) {
        val memoryUsagePercentage: Float
            get() = (usedHeapSize.toFloat() / maxHeapSize.toFloat()) * 100f
    }
}