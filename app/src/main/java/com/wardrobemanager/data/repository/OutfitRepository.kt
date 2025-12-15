package com.wardrobemanager.data.repository

import com.wardrobemanager.data.model.Outfit
import kotlinx.coroutines.flow.Flow

interface OutfitRepository {
    suspend fun insertOutfit(outfit: Outfit): Long
    suspend fun updateOutfit(outfit: Outfit)
    suspend fun deleteOutfit(outfit: Outfit)
    suspend fun getOutfitById(id: Long): Outfit?
    fun getAllOutfits(): Flow<List<Outfit>>
    fun searchOutfits(query: String): Flow<List<Outfit>>
    fun getFilteredOutfits(
        minRating: Float? = null,
        maxRating: Float? = null,
        sortByRating: Boolean = false
    ): Flow<List<Outfit>>
    
    // Outfit-Clothing relationship management
    suspend fun addClothingItemToOutfit(outfitId: Long, clothingItemId: Long)
    suspend fun removeClothingItemFromOutfit(outfitId: Long, clothingItemId: Long)
    suspend fun updateOutfitClothingItems(outfitId: Long, clothingItemIds: List<Long>)
    suspend fun isClothingItemInOutfit(outfitId: Long, clothingItemId: Long): Boolean
    
    // Statistics
    suspend fun getOutfitCount(): Int
    suspend fun getAverageOutfitRating(): Float?
    suspend fun getMostRecentlyWornOutfits(limit: Int = 10): List<Outfit>
    suspend fun getLeastRecentlyWornOutfits(limit: Int = 10): List<Outfit>
}