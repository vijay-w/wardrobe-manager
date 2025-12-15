package com.wardrobemanager.data.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ErrorHandlingPropertyTest : StringSpec({

    lateinit var context: Context
    lateinit var clothingRepository: TestClothingRepository
    lateinit var outfitRepository: TestOutfitRepository
    lateinit var backupManager: BackupManager

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        clothingRepository = TestClothingRepository()
        outfitRepository = TestOutfitRepository()
        backupManager = BackupManager(context, clothingRepository, outfitRepository)
    }

    "**Feature: wardrobe-manager, Property 18: 错误处理数据保护性**" {
        runTest {
            checkAll(100, errorScenarioGenerator()) { (originalData, errorType) ->
                // 设置初始数据
                val (clothingItems, outfits) = originalData
                clothingRepository.setData(clothingItems)
                outfitRepository.setData(outfits)
                
                // 记录原始数据状态
                val originalClothingCount = clothingRepository.getAllClothingSync().size
                val originalOutfitCount = outfitRepository.getAllOutfitsSync().size
                val originalClothingData = clothingRepository.getAllClothingSync().toList()
                val originalOutfitData = outfitRepository.getAllOutfitsSync().toList()
                
                when (errorType) {
                    ErrorType.BACKUP_FAILURE -> {
                        // 模拟备份失败场景（通过让repository抛出异常）
                        clothingRepository.setShouldFailOnGetAll(true)
                        
                        try {
                            val result = backupManager.createBackup()
                            
                            // 验证备份失败但数据未受影响
                            result.shouldBeInstanceOf<BackupResult.Error>()
                            
                            // 重置repository状态以验证数据
                            clothingRepository.setShouldFailOnGetAll(false)
                            
                            // 验证原始数据完全不变
                            clothingRepository.getAllClothingSync().size shouldBe originalClothingCount
                            outfitRepository.getAllOutfitsSync().size shouldBe originalOutfitCount
                            clothingRepository.getAllClothingSync() shouldBe originalClothingData
                            outfitRepository.getAllOutfitsSync() shouldBe originalOutfitData
                            
                        } finally {
                            clothingRepository.setShouldFailOnGetAll(false)
                        }
                    }
                    
                    ErrorType.RESTORE_FAILURE -> {
                        // 模拟恢复失败场景（损坏的备份文件）
                        val corruptBackupFile = File(context.cacheDir, "corrupt_backup.zip")
                        corruptBackupFile.writeText("这不是一个有效的ZIP文件")
                        
                        try {
                            val result = backupManager.restoreBackup(corruptBackupFile)
                            
                            // 验证恢复失败但数据未受影响
                            result.shouldBeInstanceOf<RestoreResult.Error>()
                            
                            // 验证原始数据完全不变
                            clothingRepository.getAllClothingSync().size shouldBe originalClothingCount
                            outfitRepository.getAllOutfitsSync().size shouldBe originalOutfitCount
                            clothingRepository.getAllClothingSync() shouldBe originalClothingData
                            outfitRepository.getAllOutfitsSync() shouldBe originalOutfitData
                            
                        } finally {
                            corruptBackupFile.delete()
                        }
                    }
                    
                    ErrorType.PARTIAL_RESTORE_FAILURE -> {
                        // 模拟部分恢复失败场景
                        // 创建一个有效的备份
                        val backupResult = backupManager.createBackup()
                        backupResult.shouldBeInstanceOf<BackupResult.Success>()
                        
                        val backupFile = (backupResult as BackupResult.Success).backupFile
                        
                        try {
                            // 模拟在恢复过程中发生错误（通过让repository抛出异常）
                            clothingRepository.setShouldFailOnInsert(true)
                            
                            val result = backupManager.restoreBackup(backupFile)
                            
                            // 验证恢复失败
                            result.shouldBeInstanceOf<RestoreResult.Error>()
                            
                            // 验证原始数据状态保持不变（事务回滚）
                            clothingRepository.setShouldFailOnInsert(false)
                            clothingRepository.getAllClothingSync().size shouldBe originalClothingCount
                            outfitRepository.getAllOutfitsSync().size shouldBe originalOutfitCount
                            
                        } finally {
                            clothingRepository.setShouldFailOnInsert(false)
                            backupFile.delete()
                        }
                    }
                }
            }
        }
    }

}) {
    companion object {
        enum class ErrorType {
            BACKUP_FAILURE,
            RESTORE_FAILURE,
            PARTIAL_RESTORE_FAILURE
        }
        
        fun errorScenarioGenerator() = arbitrary { rs ->
            val clothingItems = (1..Arb.int(1..3).bind()).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "测试衣服 $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = null,
                    createdAt = System.currentTimeMillis() - (index * 1000),
                    lastWorn = null
                )
            }
            
            val outfits = (1..Arb.int(1..2).bind()).map { index ->
                Outfit(
                    id = index.toLong(),
                    name = "测试穿搭 $index",
                    description = "描述 $index",
                    rating = Arb.float(0f..5f).bind(),
                    clothingItems = clothingItems.take(1),
                    createdAt = System.currentTimeMillis() - (index * 1000),
                    lastWorn = null
                )
            }
            
            val errorType = Arb.enum<ErrorType>().bind()
            
            Pair(Pair(clothingItems, outfits), errorType)
        }
    }
}

// 扩展测试Repository以支持错误模拟
class TestClothingRepository : ClothingRepository {
    private var clothingItems = mutableListOf<ClothingItem>()
    private var shouldFailOnInsert = false
    private var shouldFailOnGetAll = false
    
    fun setData(items: List<ClothingItem>) {
        clothingItems.clear()
        clothingItems.addAll(items)
    }
    
    fun clearData() {
        clothingItems.clear()
    }
    
    fun getAllClothingSync(): List<ClothingItem> = clothingItems.toList()
    
    fun setShouldFailOnInsert(shouldFail: Boolean) {
        shouldFailOnInsert = shouldFail
    }
    
    fun setShouldFailOnGetAll(shouldFail: Boolean) {
        shouldFailOnGetAll = shouldFail
    }
    
    override suspend fun insertClothing(clothing: ClothingItem): Long {
        if (shouldFailOnInsert) {
            throw IOException("模拟插入失败")
        }
        val newItem = clothing.copy(id = (clothingItems.maxOfOrNull { it.id } ?: 0) + 1)
        clothingItems.add(newItem)
        return newItem.id
    }
    
    override suspend fun updateClothing(clothing: ClothingItem) {
        val index = clothingItems.indexOfFirst { it.id == clothing.id }
        if (index >= 0) {
            clothingItems[index] = clothing
        }
    }
    
    override suspend fun deleteClothing(clothing: ClothingItem) {
        clothingItems.removeIf { it.id == clothing.id }
    }
    
    override suspend fun getClothingById(id: Long): ClothingItem? {
        return clothingItems.find { it.id == id }
    }
    
    override fun getAllClothing() = if (shouldFailOnGetAll) {
        throw IOException("模拟获取数据失败")
    } else {
        flowOf(clothingItems.toList())
    }
    
    override fun getClothingByCategory(category: ClothingCategory) = 
        flowOf(clothingItems.filter { it.category == category })
    
    override fun searchClothing(query: String) = 
        flowOf(clothingItems.filter { it.name.contains(query, ignoreCase = true) })
    
    override fun getFilteredClothing(
        category: ClothingCategory?,
        minRating: Float?,
        maxRating: Float?,
        minPrice: Double?,
        maxPrice: Double?,
        sortByRating: Boolean
    ) = flowOf(clothingItems.filter { item ->
        (category == null || item.category == category) &&
        (minRating == null || item.rating >= minRating) &&
        (maxRating == null || item.rating <= maxRating) &&
        (minPrice == null || (item.price ?: 0.0) >= minPrice) &&
        (maxPrice == null || (item.price ?: Double.MAX_VALUE) <= maxPrice)
    }.let { filtered ->
        if (sortByRating) filtered.sortedByDescending { it.rating } else filtered
    })
    
    override suspend fun getClothingCount(): Int = clothingItems.size
    
    override suspend fun getClothingCountByCategory(category: ClothingCategory): Int = 
        clothingItems.count { it.category == category }
    
    override suspend fun getAverageRating(): Float? = 
        if (clothingItems.isEmpty()) null else clothingItems.map { it.rating }.average().toFloat()
    
    override suspend fun getTotalValue(): Double? = 
        clothingItems.mapNotNull { it.price }.takeIf { it.isNotEmpty() }?.sum()
    
    override suspend fun getMostRecentlyWornItems(limit: Int): List<ClothingItem> = 
        clothingItems.filter { it.lastWorn != null }
            .sortedByDescending { it.lastWorn }
            .take(limit)
    
    override suspend fun getLeastRecentlyWornItems(limit: Int): List<ClothingItem> = 
        clothingItems.filter { it.lastWorn != null }
            .sortedBy { it.lastWorn }
            .take(limit)
}

class TestOutfitRepository : OutfitRepository {
    private var outfits = mutableListOf<Outfit>()
    
    fun setData(items: List<Outfit>) {
        outfits.clear()
        outfits.addAll(items)
    }
    
    fun clearData() {
        outfits.clear()
    }
    
    fun getAllOutfitsSync(): List<Outfit> = outfits.toList()
    
    override suspend fun insertOutfit(outfit: Outfit): Long {
        val newOutfit = outfit.copy(id = (outfits.maxOfOrNull { it.id } ?: 0) + 1)
        outfits.add(newOutfit)
        return newOutfit.id
    }
    
    override suspend fun updateOutfit(outfit: Outfit) {
        val index = outfits.indexOfFirst { it.id == outfit.id }
        if (index >= 0) {
            outfits[index] = outfit
        }
    }
    
    override suspend fun deleteOutfit(outfit: Outfit) {
        outfits.removeIf { it.id == outfit.id }
    }
    
    override suspend fun getOutfitById(id: Long): Outfit? {
        return outfits.find { it.id == id }
    }
    
    override fun getAllOutfits() = flowOf(outfits.toList())
    
    override fun searchOutfits(query: String) = 
        flowOf(outfits.filter { it.name.contains(query, ignoreCase = true) })
    
    override fun getFilteredOutfits(
        minRating: Float?,
        maxRating: Float?,
        sortByRating: Boolean
    ) = flowOf(outfits.filter { outfit ->
        (minRating == null || outfit.rating >= minRating) &&
        (maxRating == null || outfit.rating <= maxRating)
    }.let { filtered ->
        if (sortByRating) filtered.sortedByDescending { it.rating } else filtered
    })
    
    override suspend fun addClothingItemToOutfit(outfitId: Long, clothingItemId: Long) {
        // 简化实现
    }
    
    override suspend fun removeClothingItemFromOutfit(outfitId: Long, clothingItemId: Long) {
        // 简化实现
    }
    
    override suspend fun updateOutfitClothingItems(outfitId: Long, clothingItemIds: List<Long>) {
        // 简化实现
    }
    
    override suspend fun isClothingItemInOutfit(outfitId: Long, clothingItemId: Long): Boolean = false
    
    override suspend fun getOutfitCount(): Int = outfits.size
    
    override suspend fun getAverageOutfitRating(): Float? = 
        if (outfits.isEmpty()) null else outfits.map { it.rating }.average().toFloat()
    
    override suspend fun getMostRecentlyWornOutfits(limit: Int): List<Outfit> = 
        outfits.filter { it.lastWorn != null }
            .sortedByDescending { it.lastWorn }
            .take(limit)
    
    override suspend fun getLeastRecentlyWornOutfits(limit: Int): List<Outfit> = 
        outfits.filter { it.lastWorn != null }
            .sortedBy { it.lastWorn }
            .take(limit)
}