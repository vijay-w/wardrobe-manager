package com.wardrobemanager.ui.wardrobe

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem

data class WardrobeUiState(
    val clothingItems: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: ClothingCategory? = null,
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortByRating: Boolean = false,
    val isFilterExpanded: Boolean = false
)

data class FilterOptions(
    val category: ClothingCategory? = null,
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortByRating: Boolean = false
)