package com.wardrobemanager.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    private val imagesDir: File by lazy {
        File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveImage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open input stream for URI: $uri")
            
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageFile = File(imagesDir, fileName)
            
            inputStream.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Compress the saved image
            compressImage(imageFile.absolutePath)
        } catch (e: Exception) {
            throw IOException("Failed to save image: ${e.message}", e)
        }
    }

    override suspend fun saveImageFromCamera(imageFile: File): String = withContext(Dispatchers.IO) {
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val destinationFile = File(imagesDir, fileName)
            
            imageFile.copyTo(destinationFile, overwrite = true)
            
            // Compress the saved image
            compressImage(destinationFile.absolutePath)
        } catch (e: Exception) {
            throw IOException("Failed to save camera image: ${e.message}", e)
        }
    }

    override suspend fun deleteImage(imagePath: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log error but don't throw - deletion failure shouldn't break the app
            e.printStackTrace()
        }
    }

    override fun getImageFile(imagePath: String): File? {
        return try {
            val file = File(imagePath)
            if (file.exists() && file.isFile) file else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun compressImage(imagePath: String, quality: Int): String = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(imagePath)
            if (!originalFile.exists()) return@withContext imagePath
            
            // Use efficient decoding with options to avoid OutOfMemoryError
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeFile(imagePath, options)
            
            // Calculate sample size for memory efficiency
            val maxDimension = 1024
            options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
                ?: return@withContext imagePath
            
            // Calculate new dimensions to keep image under reasonable size
            val (newWidth, newHeight) = calculateNewDimensions(
                bitmap.width, 
                bitmap.height, 
                maxDimension
            )
            
            val resizedBitmap = if (newWidth != bitmap.width || newHeight != bitmap.height) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                    bitmap.recycle() // Recycle original immediately
                }
            } else {
                bitmap
            }
            
            // Save compressed image
            FileOutputStream(originalFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            // Clean up bitmap
            resizedBitmap.recycle()
            
            imagePath
        } catch (e: Exception) {
            throw IOException("Failed to compress image: ${e.message}", e)
        }
    }

    override suspend fun getImageSize(imagePath: String): Long = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun cleanupUnusedImages(usedImagePaths: List<String>) = withContext(Dispatchers.IO) {
        try {
            val usedFileNames = usedImagePaths.map { path ->
                File(path).name
            }.toSet()
            
            imagesDir.listFiles()?.forEach { file ->
                if (file.isFile && !usedFileNames.contains(file.name)) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Log error but don't throw
            e.printStackTrace()
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

    private fun calculateNewDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxDimension: Int
    ): Pair<Int, Int> {
        if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
            return Pair(originalWidth, originalHeight)
        }
        
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return if (originalWidth > originalHeight) {
            val newWidth = maxDimension
            val newHeight = (maxDimension / aspectRatio).toInt()
            Pair(newWidth, newHeight)
        } else {
            val newHeight = maxDimension
            val newWidth = (maxDimension * aspectRatio).toInt()
            Pair(newWidth, newHeight)
        }
    }
}