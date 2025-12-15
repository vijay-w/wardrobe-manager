package com.wardrobemanager.data.backup

import android.content.Context
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val context: Context,
    private val clothingRepository: ClothingRepository,
    private val outfitRepository: OutfitRepository
) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    suspend fun createBackup(
        onProgress: (BackupProgress) -> Unit = {}
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            onProgress(BackupProgress(0, "开始备份..."))
            
            // Get all data
            onProgress(BackupProgress(10, "获取衣服数据..."))
            val clothingItems = clothingRepository.getAllClothing().first()
            
            onProgress(BackupProgress(30, "获取穿搭数据..."))
            val outfits = outfitRepository.getAllOutfits().first()
            
            // Create backup data structure
            onProgress(BackupProgress(50, "准备备份数据..."))
            val backupData = BackupData(
                version = BACKUP_VERSION,
                timestamp = System.currentTimeMillis(),
                clothingItems = clothingItems,
                outfits = outfits
            )
            
            // Create backup file
            onProgress(BackupProgress(70, "创建备份文件..."))
            val backupFile = createBackupFile()
            
            // Write data to zip file
            onProgress(BackupProgress(80, "写入数据..."))
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // Add JSON data
                val jsonData = json.encodeToString(backupData)
                val jsonEntry = ZipEntry("data.json")
                zipOut.putNextEntry(jsonEntry)
                zipOut.write(jsonData.toByteArray())
                zipOut.closeEntry()
                
                // Add images
                onProgress(BackupProgress(90, "备份图片..."))
                backupImages(zipOut, clothingItems)
            }
            
            onProgress(BackupProgress(100, "备份完成"))
            BackupResult.Success(backupFile)
            
        } catch (e: Exception) {
            BackupResult.Error("备份失败: ${e.message}")
        }
    }
    
    suspend fun restoreBackup(
        backupFile: File,
        onProgress: (RestoreProgress) -> Unit = {}
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            onProgress(RestoreProgress(0, "开始恢复..."))
            
            if (!backupFile.exists()) {
                return@withContext RestoreResult.Error("备份文件不存在")
            }
            
            // Extract and parse backup data
            onProgress(RestoreProgress(20, "读取备份数据..."))
            val backupData = extractBackupData(backupFile)
            
            // Validate backup version
            if (backupData.version > BACKUP_VERSION) {
                return@withContext RestoreResult.Error("备份文件版本过新，请更新应用")
            }
            
            // Restore images first
            onProgress(RestoreProgress(40, "恢复图片..."))
            restoreImages(backupFile, backupData.clothingItems)
            
            // Clear existing data (optional - could be made configurable)
            onProgress(RestoreProgress(60, "清理现有数据..."))
            // Note: In a real implementation, you might want to ask user about this
            
            // Restore clothing items
            onProgress(RestoreProgress(70, "恢复衣服数据..."))
            backupData.clothingItems.forEach { item ->
                clothingRepository.insertClothing(item)
            }
            
            // Restore outfits
            onProgress(RestoreProgress(90, "恢复穿搭数据..."))
            backupData.outfits.forEach { outfit ->
                outfitRepository.insertOutfit(outfit)
            }
            
            onProgress(RestoreProgress(100, "恢复完成"))
            RestoreResult.Success(backupData.clothingItems.size, backupData.outfits.size)
            
        } catch (e: Exception) {
            RestoreResult.Error("恢复失败: ${e.message}")
        }
    }
    
    private fun createBackupFile(): File {
        val backupDir = File(context.getExternalFilesDir(null), "backups").apply {
            if (!exists()) mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        
        return File(backupDir, "wardrobe_backup_$timestamp.zip")
    }
    
    private fun backupImages(zipOut: ZipOutputStream, clothingItems: List<ClothingItem>) {
        clothingItems.forEach { item ->
            try {
                val imageFile = File(item.imagePath)
                if (imageFile.exists()) {
                    val entry = ZipEntry("images/${imageFile.name}")
                    zipOut.putNextEntry(entry)
                    
                    FileInputStream(imageFile).use { input ->
                        input.copyTo(zipOut)
                    }
                    
                    zipOut.closeEntry()
                }
            } catch (e: Exception) {
                // Log error but continue with other images
                e.printStackTrace()
            }
        }
    }
    
    private fun extractBackupData(backupFile: File): BackupData {
        ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (entry.name == "data.json") {
                    val jsonData = zipIn.readBytes().toString(Charsets.UTF_8)
                    return json.decodeFromString<BackupData>(jsonData)
                }
                entry = zipIn.nextEntry
            }
        }
        throw IllegalArgumentException("备份文件格式无效")
    }
    
    private fun restoreImages(backupFile: File, clothingItems: List<ClothingItem>) {
        val imagesDir = File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
        
        ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (entry.name.startsWith("images/")) {
                    val fileName = entry.name.substringAfter("images/")
                    val outputFile = File(imagesDir, fileName)
                    
                    FileOutputStream(outputFile).use { output ->
                        zipIn.copyTo(output)
                    }
                }
                entry = zipIn.nextEntry
            }
        }
    }
    
    fun getBackupFiles(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles { file ->
                file.name.startsWith("wardrobe_backup_") && file.name.endsWith(".zip")
            }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun deleteBackupFile(backupFile: File): Boolean {
        return try {
            backupFile.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getBackupFileInfo(backupFile: File): BackupFileInfo? {
        return try {
            val backupData = extractBackupData(backupFile)
            BackupFileInfo(
                file = backupFile,
                timestamp = backupData.timestamp,
                clothingItemsCount = backupData.clothingItems.size,
                outfitsCount = backupData.outfits.size,
                version = backupData.version,
                size = backupFile.length()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    companion object {
        private const val BACKUP_VERSION = 1
    }
}

@Serializable
data class BackupData(
    val version: Int,
    val timestamp: Long,
    val clothingItems: List<ClothingItem>,
    val outfits: List<Outfit>
)

data class BackupProgress(
    val percentage: Int,
    val message: String
)

data class RestoreProgress(
    val percentage: Int,
    val message: String
)

sealed class BackupResult {
    data class Success(val backupFile: File) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val clothingItemsCount: Int, val outfitsCount: Int) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

data class BackupFileInfo(
    val file: File,
    val timestamp: Long,
    val clothingItemsCount: Int,
    val outfitsCount: Int,
    val version: Int,
    val size: Long
)