package com.wardrobemanager.data.repository

import android.net.Uri
import java.io.File

interface ImageRepository {
    suspend fun saveImage(uri: Uri): String
    suspend fun saveImageFromCamera(imageFile: File): String
    suspend fun deleteImage(imagePath: String)
    fun getImageFile(imagePath: String): File?
    suspend fun compressImage(imagePath: String, quality: Int = 80): String
    suspend fun getImageSize(imagePath: String): Long
    suspend fun cleanupUnusedImages(usedImagePaths: List<String>)
}