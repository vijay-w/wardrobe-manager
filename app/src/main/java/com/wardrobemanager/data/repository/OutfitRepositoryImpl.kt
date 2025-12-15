package com.wardrobemanager.data.repository

import com.wardrobemanager.data.database.dao.OutfitDao
import com.wardrobemanager.data.database.entity.OutfitClothingCrossRef
import com.wardrobemanager.data.mapper.toOutfit
import com.wardrobemanager.data.mapper.toOutfitEntity
import com.wardrobemanager.data.model.Outfit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitRepositoryImpl @Inject constructor(
    private val outfitDao: OutfitDao
) : OutfitRepository {

    override suspend fun insertOutfit(outfit: Outfit): Long {
        val outfitId = outfitDao.insertOutfit(outfit.toOutfitEntity())
        
        // Insert clothing item relationships
        if (outfit.clothingItems.isNotEmpty()) {
            val crossRefs = outfit.clothingItems.map { clothingItem ->
                OutfitClothingCrossRef(
                    outfitId = outfitId,
                    clothingItemId = clothingItem.id
                )
            }
            outfitDao.insertOutfitClothingCrossRefs(crossRefs)
        }
        
        return outfitId
    }

    override suspend fun updateOutfit(outfit: Outfit) {
        outfitDao.updateOutfit(outfit.toOutfitEntity())
        
        // Update clothing item relationships
        outfitDao.deleteAllClothingItemsFromOutfit(outfit.id)
        if (outfit.clothingItems.isNotEmpty()) {
            val crossRefs = outfit.clothingItems.map { clothingItem ->
                OutfitClothingCrossRef(
                    outfitId = outfit.id,
                    clothingItemId = clothingItem.id
                )
            }
            outfitDao.insertOutfitClothingCrossRefs(crossRefs)
        }
    }

    override suspend fun deleteOutfit(outfit: Outfit) {
        outfitDao.deleteOutfit(outfit.toOutfitEntity())
    }

    override suspend fun getOutfitById(id: Long): Outfit? {
        return outfitDao.getOutfitWithClothingItemsById(id)?.toOutfit()
    }

    override fun getAllOutfits(): Flow<List<Outfit>> {
        return outfitDao.getAllOutfitsWithClothingItems().map { outfitsWithItems ->
            outfitsWithItems.map { it.toOutfit() }
        }
    }

    override fun searchOutfits(query: String): Flow<List<Outfit>> {
        return outfitDao.searchOutfitsWithClothingItems(query).map { outfitsWithItems ->
            outfitsWithItems.map { it.toOutfit() }
        }
    }

    override fun getFilteredOutfits(
        minRating: Float?,
        maxRating: Float?,
        sortByRating: Boolean
    ): Flow<List<Outfit>> {
        return outfitDao.getFilteredOutfitsWithClothingItems(
            minRating = minRating,
            maxRating = maxRating,
            sortByRating = sortByRating
        ).map { outfitsWithItems ->
            outfitsWithItems.map { it.toOutfit() }
        }
    }

    override suspend fun addClothingItemToOutfit(outfitId: Long, clothingItemId: Long) {
        val crossRef = OutfitClothingCrossRef(
            outfitId = outfitId,
            clothingItemId = clothingItemId
        )
        outfitDao.insertOutfitClothingCrossRef(crossRef)
    }

    override suspend fun removeClothingItemFromOutfit(outfitId: Long, clothingItemId: Long) {
        outfitDao.removeClothingItemFromOutfit(outfitId, clothingItemId)
    }

    override suspend fun updateOutfitClothingItems(outfitId: Long, clothingItemIds: List<Long>) {
        // Remove all existing relationships
        outfitDao.deleteAllClothingItemsFromOutfit(outfitId)
        
        // Add new relationships
        if (clothingItemIds.isNotEmpty()) {
            val crossRefs = clothingItemIds.map { clothingItemId ->
                OutfitClothingCrossRef(
                    outfitId = outfitId,
                    clothingItemId = clothingItemId
                )
            }
            outfitDao.insertOutfitClothingCrossRefs(crossRefs)
        }
    }

    override suspend fun isClothingItemInOutfit(outfitId: Long, clothingItemId: Long): Boolean {
        return outfitDao.isClothingItemInOutfit(outfitId, clothingItemId) > 0
    }

    override suspend fun getOutfitCount(): Int {
        return outfitDao.getOutfitCount()
    }

    override suspend fun getAverageOutfitRating(): Float? {
        return outfitDao.getAverageOutfitRating()
    }

    override suspend fun getMostRecentlyWornOutfits(limit: Int): List<Outfit> {
        return outfitDao.getMostRecentlyWornOutfits(limit).map { it.toOutfit() }
    }

    override suspend fun getLeastRecentlyWornOutfits(limit: Int): List<Outfit> {
        return outfitDao.getLeastRecentlyWornOutfits(limit).map { it.toOutfit() }
    }
}