package com.wardrobemanager.data.database.dao

import androidx.room.*
import com.wardrobemanager.data.database.entity.OutfitEntity
import com.wardrobemanager.data.database.entity.OutfitClothingCrossRef
import com.wardrobemanager.data.database.entity.OutfitWithClothingItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun getAllOutfits(): Flow<List<OutfitEntity>>
    
    @Transaction
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun getAllOutfitsWithClothingItems(): Flow<List<OutfitWithClothingItems>>
    
    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getOutfitById(id: Long): OutfitEntity?
    
    @Transaction
    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getOutfitWithClothingItemsById(id: Long): OutfitWithClothingItems?
    
    @Query("""
        SELECT * FROM outfits 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchOutfits(query: String): Flow<List<OutfitEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM outfits 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchOutfitsWithClothingItems(query: String): Flow<List<OutfitWithClothingItems>>
    
    @Query("""
        SELECT * FROM outfits 
        WHERE (:minRating IS NULL OR rating >= :minRating)
        AND (:maxRating IS NULL OR rating <= :maxRating)
        ORDER BY 
        CASE WHEN :sortByRating = 1 THEN rating END DESC,
        CASE WHEN :sortByRating = 0 THEN createdAt END DESC
    """)
    fun getFilteredOutfits(
        minRating: Float? = null,
        maxRating: Float? = null,
        sortByRating: Boolean = false
    ): Flow<List<OutfitEntity>>
    
    @Transaction
    @Query("""
        SELECT * FROM outfits 
        WHERE (:minRating IS NULL OR rating >= :minRating)
        AND (:maxRating IS NULL OR rating <= :maxRating)
        ORDER BY 
        CASE WHEN :sortByRating = 1 THEN rating END DESC,
        CASE WHEN :sortByRating = 0 THEN createdAt END DESC
    """)
    fun getFilteredOutfitsWithClothingItems(
        minRating: Float? = null,
        maxRating: Float? = null,
        sortByRating: Boolean = false
    ): Flow<List<OutfitWithClothingItems>>
    
    @Query("SELECT COUNT(*) FROM outfits")
    suspend fun getOutfitCount(): Int
    
    @Query("SELECT AVG(rating) FROM outfits WHERE rating > 0")
    suspend fun getAverageOutfitRating(): Float?
    
    @Query("""
        SELECT * FROM outfits 
        WHERE lastWorn IS NOT NULL 
        ORDER BY lastWorn DESC 
        LIMIT :limit
    """)
    suspend fun getMostRecentlyWornOutfits(limit: Int = 10): List<OutfitEntity>
    
    @Query("""
        SELECT * FROM outfits 
        WHERE lastWorn IS NULL OR lastWorn = (
            SELECT MIN(lastWorn) FROM outfits WHERE lastWorn IS NOT NULL
        )
        LIMIT :limit
    """)
    suspend fun getLeastRecentlyWornOutfits(limit: Int = 10): List<OutfitEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: OutfitEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfits(outfits: List<OutfitEntity>): List<Long>
    
    @Update
    suspend fun updateOutfit(outfit: OutfitEntity)
    
    @Delete
    suspend fun deleteOutfit(outfit: OutfitEntity)
    
    @Query("DELETE FROM outfits WHERE id = :id")
    suspend fun deleteOutfitById(id: Long)
    
    @Query("DELETE FROM outfits")
    suspend fun deleteAllOutfits()
    
    // Cross-reference operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfitClothingCrossRef(crossRef: OutfitClothingCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfitClothingCrossRefs(crossRefs: List<OutfitClothingCrossRef>)
    
    @Delete
    suspend fun deleteOutfitClothingCrossRef(crossRef: OutfitClothingCrossRef)
    
    @Query("DELETE FROM outfit_clothing_cross_ref WHERE outfitId = :outfitId")
    suspend fun deleteAllClothingItemsFromOutfit(outfitId: Long)
    
    @Query("DELETE FROM outfit_clothing_cross_ref WHERE outfitId = :outfitId AND clothingItemId = :clothingItemId")
    suspend fun removeClothingItemFromOutfit(outfitId: Long, clothingItemId: Long)
    
    @Query("""
        SELECT COUNT(*) FROM outfit_clothing_cross_ref 
        WHERE outfitId = :outfitId AND clothingItemId = :clothingItemId
    """)
    suspend fun isClothingItemInOutfit(outfitId: Long, clothingItemId: Long): Int
}