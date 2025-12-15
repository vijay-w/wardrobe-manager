package com.wardrobemanager.data.repository

import com.wardrobemanager.data.database.dao.ClothingItemDao
import com.wardrobemanager.data.mapper.toClothingItem
import com.wardrobemanager.data.mapper.toClothingItemEntity
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.ui.error.RepositoryErrorHandler
import com.wardrobemanager.ui.error.withDatabaseErrorHandling
import com.wardrobemanager.ui.error.withErrorHandling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClothingRepositoryImpl @Inject constructor(
    private val clothingItemDao: ClothingItemDao,
    private val errorHandler: RepositoryErrorHandler
) : ClothingRepository {

    override suspend fun insertClothing(clothing: ClothingItem): Long {
        return errorHandler.withDatabaseErrorHandling("insertClothing") {
            clothingItemDao.insertClothingItem(clothing.toClothingItemEntity())
        }
    }

    override suspend fun updateClothing(clothing: ClothingItem) {
        errorHandler.withDatabaseErrorHandling("updateClothing") {
            clothingItemDao.updateClothingItem(clothing.toClothingItemEntity())
        }
    }

    override suspend fun deleteClothing(clothing: ClothingItem) {
        errorHandler.withDatabaseErrorHandling("deleteClothing") {
            clothingItemDao.deleteClothingItem(clothing.toClothingItemEntity())
        }
    }

    override suspend fun getClothingById(id: Long): ClothingItem? {
        return errorHandler.withDatabaseErrorHandling("getClothingById") {
            clothingItemDao.getClothingItemById(id)?.toClothingItem()
        }
    }

    override fun getAllClothing(): Flow<List<ClothingItem>> {
        return clothingItemDao.getAllClothingItems()
            .map { entities ->
                entities.map { it.toClothingItem() }
            }
            .withErrorHandling(errorHandler, "getAllClothing")
    }

    override fun getClothingByCategory(category: ClothingCategory): Flow<List<ClothingItem>> {
        return clothingItemDao.getClothingItemsByCategory(category)
            .map { entities ->
                entities.map { it.toClothingItem() }
            }
            .withErrorHandling(errorHandler, "getClothingByCategory")
    }

    override fun searchClothing(query: String): Flow<List<ClothingItem>> {
        return clothingItemDao.searchClothingItems(query)
            .map { entities ->
                entities.map { it.toClothingItem() }
            }
            .withErrorHandling(errorHandler, "searchClothing")
    }

    override fun getFilteredClothing(
        category: ClothingCategory?,
        minRating: Float?,
        maxRating: Float?,
        minPrice: Double?,
        maxPrice: Double?,
        sortByRating: Boolean
    ): Flow<List<ClothingItem>> {
        return clothingItemDao.getFilteredClothingItems(
            category = category,
            minRating = minRating,
            maxRating = maxRating,
            minPrice = minPrice,
            maxPrice = maxPrice,
            sortByRating = sortByRating
        ).map { entities ->
            entities.map { it.toClothingItem() }
        }
    }

    override suspend fun getClothingCount(): Int {
        return clothingItemDao.getClothingItemCount()
    }

    override suspend fun getClothingCountByCategory(category: ClothingCategory): Int {
        return clothingItemDao.getClothingItemCountByCategory(category)
    }

    override suspend fun getAverageRating(): Float? {
        return clothingItemDao.getAverageRating()
    }

    override suspend fun getTotalValue(): Double? {
        return clothingItemDao.getTotalValue()
    }

    override suspend fun getMostRecentlyWornItems(limit: Int): List<ClothingItem> {
        return clothingItemDao.getMostRecentlyWornItems(limit).map { it.toClothingItem() }
    }

    override suspend fun getLeastRecentlyWornItems(limit: Int): List<ClothingItem> {
        return clothingItemDao.getLeastRecentlyWornItems(limit).map { it.toClothingItem() }
    }
}