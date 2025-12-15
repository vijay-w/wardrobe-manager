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
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.zip.ZipInputStream
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
class BackupManagerPropertyTest : StringSpec({

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

    "**Feature: wardrobe-manager, Property 17: 数据备份往返一致性**" {
        runTest {
            checkAll(100, backupTestDataGenerator()) { (originalClothingItems, originalOutfits) ->
                // 设置初始数据
                clothingRepository.setData(originalClothingItems)
                outfitRepository.setData(originalOutfits)
                
                // 创建备份
                val backupResult = backupManager.createBackup()
                backupResult.shouldBeInstanceOf<BackupResult.Success>()
                
                val backupFile = (backupResult as BackupResult.Success).backupFile
                
                try {
                    // 验证备份文件包含正确的数据
                    val backupData = extractBackupDataFromFile(backupFile)
                    backupData.clothingItems.size shouldBe originalClothingItems.size
                    backupData.outfits.size shouldBe originalOutfits.size
                    
                    // 清空仓库数据以模拟恢复场景
                    clothingRepository.clearData()
                    outfitRepository.clearData()
                    
                    // 从备份恢复
                    val restoreResult = backupManager.restoreBackup(backupFile)
                    restoreResult.shouldBeInstanceOf<RestoreResult.Success>()
                    
                    val restoreSuccess = restoreResult as RestoreResult.Success
                    
                    // 验证恢复的数据数量正确
                    restoreSuccess.clothingItemsCount shouldBe originalClothingItems.size
                    restoreSuccess.outfitsCount shouldBe originalOutfits.size
                    
                    // 验证恢复后的数据与原始数据一致
                    val restoredClothingItems = clothingRepository.getAllClothingSync()
                    val restoredOutfits = outfitRepository.getAllOutfitsSync()
                    
                    restoredClothingItems.size shouldBe originalClothingItems.size
                    restoredOutfits.size shouldBe originalOutfits.size
                    
                    // 验证每个衣服项目的数据一致性（忽略ID，因为可能会重新生成）
                    originalClothingItems.forEachIndexed { index, original ->
                        val restored = restoredClothingItems.find { it.name == original.name }
                        restored shouldBe original.copy(id = restored?.id ?: 0)
                    }
                    
                    // 验证每个穿搭的数据一致性（忽略ID）
                    originalOutfits.forEachIndexed { index, original ->
                        val restored = restoredOutfits.find { it.name == original.name }
                        restored?.name shouldBe original.name
                        restored?.description shouldBe original.description
                        restored?.rating shouldBe original.rating
                        restored?.clothingItems?.size shouldBe original.clothingItems.size
                    }
                    
                } finally {
                    // 清理备份文件
                    backupFile.delete()
                }
            }
        }
    }

}) {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        
        fun extractBackupDataFromFile(backupFile: File): BackupData {
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
        
        fun backupTestDataGenerator() = arbitrary { rs ->
            val clothingItems = (1..Arb.int(1..5).bind()).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "测试衣服 $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = Arb.string(10..100).orNull().bind(),
                    notes = Arb.string(0..200).orNull().bind(),
                    createdAt = System.currentTimeMillis() - (index * 1000),
                    lastWorn = null
                )
            }
            
            val outfits = (1..Arb.int(1..3).bind()).map { index ->
                Outfit(
                    id = index.toLong(),
                    name = "测试穿搭 $index",
                    description = "描述 $index",
                    rating = Arb.float(0f..5f).bind(),
                    clothingItems = clothingItems.take(Arb.int(1..clothingItems.size).bind()),
                    createdAt = System.currentTimeMillis() - (index * 1000),
                    lastWorn = null
                )
            }
            
            Pair(clothingItems, outfits)
        }
    }
}

// 测试用的Repository实现
class TestClothingRepository : ClothingRepository {
    private var clothingItems = mutableListOf<ClothingItem>()
    
    fun setData(items: List<ClothingItem>) {
        clothingItems.clear()
        clothingItems.addAll(items)
    }
    
    fun clearData() {
        clothingItems.clear()
    }
    
    fun getAllClothingSync(): List<ClothingItem> = clothingItems.toList()
    
    override suspend fun insertClothing(clothing: ClothingItem): Long {
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
    
    override fun getAllClothing() = flowOf(clothingItems.toList())
    
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
        // 简化实现，实际应用中需要更复杂的逻辑
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