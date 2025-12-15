package com.wardrobemanager.data.repository

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import kotlinx.coroutines.flow.Flow

interface ClothingRepository {
    suspend fun insertClothing(clothing: ClothingItem): Long
    suspend fun updateClothing(clothing: ClothingItem)
    suspend fun deleteClothing(clothing: ClothingItem)
    suspend fun getClothingById(id: Long): ClothingItem?
    fun getAllClothing(): Flow<List<ClothingItem>>
    fun getClothingByCategory(category: ClothingCategory): Flow<List<ClothingItem>>
    fun searchClothing(query: String): Flow<List<ClothingItem>>
    fun getFilteredClothing(
        category: ClothingCategory? = null,
        minRating: Float? = null,
        maxRating: Float? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortByRating: Boolean = false
    ): Flow<List<ClothingItem>>
    
    // Statistics
    suspend fun getClothingCount(): Int
    suspend fun getClothingCountByCategory(category: ClothingCategory): Int
    suspend fun getAverageRating(): Float?
    suspend fun getTotalValue(): Double?
    suspend fun getMostRecentlyWornItems(limit: Int = 10): List<ClothingItem>
    suspend fun getLeastRecentlyWornItems(limit: Int = 10): List<ClothingItem>
}