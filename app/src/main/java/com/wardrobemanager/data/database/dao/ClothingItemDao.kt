package com.wardrobemanager.data.database.dao

import androidx.room.*
import com.wardrobemanager.data.database.entity.ClothingItemEntity
import com.wardrobemanager.data.model.ClothingCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    
    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    fun getAllClothingItems(): Flow<List<ClothingItemEntity>>
    
    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getClothingItemById(id: Long): ClothingItemEntity?
    
    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY createdAt DESC")
    fun getClothingItemsByCategory(category: ClothingCategory): Flow<List<ClothingItemEntity>>
    
    @Query("""
        SELECT * FROM clothing_items 
        WHERE name LIKE '%' || :query || '%' 
        OR notes LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchClothingItems(query: String): Flow<List<ClothingItemEntity>>
    
    @Query("""
        SELECT * FROM clothing_items 
        WHERE (:category IS NULL OR category = :category)
        AND (:minRating IS NULL OR rating >= :minRating)
        AND (:maxRating IS NULL OR rating <= :maxRating)
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        ORDER BY 
        CASE WHEN :sortByRating = 1 THEN rating END DESC,
        CASE WHEN :sortByRating = 0 THEN createdAt END DESC
    """)
    fun getFilteredClothingItems(
        category: ClothingCategory? = null,
        minRating: Float? = null,
        maxRating: Float? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortByRating: Boolean = false
    ): Flow<List<ClothingItemEntity>>
    
    @Query("SELECT COUNT(*) FROM clothing_items")
    suspend fun getClothingItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM clothing_items WHERE category = :category")
    suspend fun getClothingItemCountByCategory(category: ClothingCategory): Int
    
    @Query("SELECT AVG(rating) FROM clothing_items WHERE rating > 0")
    suspend fun getAverageRating(): Float?
    
    @Query("SELECT SUM(price) FROM clothing_items WHERE price IS NOT NULL")
    suspend fun getTotalValue(): Double?
    
    @Query("""
        SELECT * FROM clothing_items 
        WHERE lastWorn IS NOT NULL 
        ORDER BY lastWorn DESC 
        LIMIT :limit
    """)
    suspend fun getMostRecentlyWornItems(limit: Int = 10): List<ClothingItemEntity>
    
    @Query("""
        SELECT * FROM clothing_items 
        WHERE lastWorn IS NULL OR lastWorn = (
            SELECT MIN(lastWorn) FROM clothing_items WHERE lastWorn IS NOT NULL
        )
        LIMIT :limit
    """)
    suspend fun getLeastRecentlyWornItems(limit: Int = 10): List<ClothingItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItem(clothingItem: ClothingItemEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItems(clothingItems: List<ClothingItemEntity>): List<Long>
    
    @Update
    suspend fun updateClothingItem(clothingItem: ClothingItemEntity)
    
    @Delete
    suspend fun deleteClothingItem(clothingItem: ClothingItemEntity)
    
    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteClothingItemById(id: Long)
    
    @Query("DELETE FROM clothing_items")
    suspend fun deleteAllClothingItems()
}