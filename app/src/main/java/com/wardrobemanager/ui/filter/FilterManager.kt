package com.wardrobemanager.ui.filter

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterManager @Inject constructor() {
    
    fun applyFilters(
        items: List<ClothingItem>,
        filters: ClothingFilters
    ): List<ClothingItem> {
        return items.filter { item ->
            // Category filter
            (filters.categories.isEmpty() || filters.categories.contains(item.category)) &&
            
            // Rating filter
            (filters.minRating == null || item.rating >= filters.minRating) &&
            (filters.maxRating == null || item.rating <= filters.maxRating) &&
            
            // Price filter
            (filters.minPrice == null || (item.price ?: 0.0) >= filters.minPrice) &&
            (filters.maxPrice == null || (item.price ?: Double.MAX_VALUE) <= filters.maxPrice) &&
            
            // Recently worn filter
            when (filters.wornFilter) {
                WornFilter.ALL -> true
                WornFilter.RECENTLY_WORN -> item.lastWorn != null && 
                    (System.currentTimeMillis() - item.lastWorn) <= (7 * 24 * 60 * 60 * 1000) // 7 days
                WornFilter.NOT_RECENTLY_WORN -> item.lastWorn == null || 
                    (System.currentTimeMillis() - item.lastWorn) > (30 * 24 * 60 * 60 * 1000) // 30 days
            }
        }.let { filteredItems ->
            // Apply sorting
            when (filters.sortBy) {
                SortBy.DATE_ADDED -> filteredItems.sortedByDescending { it.createdAt }
                SortBy.NAME -> filteredItems.sortedBy { it.name }
                SortBy.RATING -> filteredItems.sortedByDescending { it.rating }
                SortBy.PRICE_LOW_TO_HIGH -> filteredItems.sortedBy { it.price ?: 0.0 }
                SortBy.PRICE_HIGH_TO_LOW -> filteredItems.sortedByDescending { it.price ?: 0.0 }
                SortBy.LAST_WORN -> filteredItems.sortedByDescending { it.lastWorn ?: 0L }
            }
        }
    }
    
    fun applyOutfitFilters(
        outfits: List<Outfit>,
        filters: OutfitFilters
    ): List<Outfit> {
        return outfits.filter { outfit ->
            // Rating filter
            (filters.minRating == null || outfit.rating >= filters.minRating) &&
            (filters.maxRating == null || outfit.rating <= filters.maxRating) &&
            
            // Item count filter
            (filters.minItemCount == null || outfit.clothingItems.size >= filters.minItemCount) &&
            (filters.maxItemCount == null || outfit.clothingItems.size <= filters.maxItemCount) &&
            
            // Recently worn filter
            when (filters.wornFilter) {
                WornFilter.ALL -> true
                WornFilter.RECENTLY_WORN -> outfit.lastWorn != null && 
                    (System.currentTimeMillis() - outfit.lastWorn) <= (7 * 24 * 60 * 60 * 1000)
                WornFilter.NOT_RECENTLY_WORN -> outfit.lastWorn == null || 
                    (System.currentTimeMillis() - outfit.lastWorn) > (30 * 24 * 60 * 60 * 1000)
            }
        }.let { filteredOutfits ->
            // Apply sorting
            when (filters.sortBy) {
                OutfitSortBy.DATE_CREATED -> filteredOutfits.sortedByDescending { it.createdAt }
                OutfitSortBy.NAME -> filteredOutfits.sortedBy { it.name }
                OutfitSortBy.RATING -> filteredOutfits.sortedByDescending { it.rating }
                OutfitSortBy.ITEM_COUNT -> filteredOutfits.sortedByDescending { it.clothingItems.size }
                OutfitSortBy.LAST_WORN -> filteredOutfits.sortedByDescending { it.lastWorn ?: 0L }
            }
        }
    }
    
    fun getFilterSummary(filters: ClothingFilters): String {
        val parts = mutableListOf<String>()
        
        if (filters.categories.isNotEmpty()) {
            parts.add("${filters.categories.size} 个分类")
        }
        
        if (filters.minRating != null || filters.maxRating != null) {
            val min = filters.minRating ?: 0f
            val max = filters.maxRating ?: 5f
            parts.add("评分 $min-$max 星")
        }
        
        if (filters.minPrice != null || filters.maxPrice != null) {
            val min = filters.minPrice?.let { "¥$it" } ?: "¥0"
            val max = filters.maxPrice?.let { "¥$it" } ?: "无上限"
            parts.add("价格 $min-$max")
        }
        
        if (filters.wornFilter != WornFilter.ALL) {
            parts.add(when (filters.wornFilter) {
                WornFilter.RECENTLY_WORN -> "最近穿过"
                WornFilter.NOT_RECENTLY_WORN -> "很久未穿"
                else -> ""
            })
        }
        
        return if (parts.isEmpty()) "无筛选" else parts.joinToString(", ")
    }
    
    fun resetFilters(): ClothingFilters {
        return ClothingFilters()
    }
    
    fun resetOutfitFilters(): OutfitFilters {
        return OutfitFilters()
    }
}

data class ClothingFilters(
    val categories: Set<ClothingCategory> = emptySet(),
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val wornFilter: WornFilter = WornFilter.ALL,
    val sortBy: SortBy = SortBy.DATE_ADDED
)

data class OutfitFilters(
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val minItemCount: Int? = null,
    val maxItemCount: Int? = null,
    val wornFilter: WornFilter = WornFilter.ALL,
    val sortBy: OutfitSortBy = OutfitSortBy.DATE_CREATED
)

enum class WornFilter {
    ALL,
    RECENTLY_WORN,
    NOT_RECENTLY_WORN
}

enum class SortBy {
    DATE_ADDED,
    NAME,
    RATING,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    LAST_WORN
}

enum class OutfitSortBy {
    DATE_CREATED,
    NAME,
    RATING,
    ITEM_COUNT,
    LAST_WORN
}