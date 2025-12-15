package com.wardrobemanager.ui.outfit

import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit

data class OutfitListUiState(
    val outfits: List<Outfit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val minRating: Float? = null,
    val maxRating: Float? = null,
    val sortByRating: Boolean = false
)

data class OutfitDetailUiState(
    val outfit: Outfit? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
)

data class CreateOutfitUiState(
    val name: String = "",
    val description: String = "",
    val selectedClothingItems: List<ClothingItem> = emptyList(),
    val availableClothingItems: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null
)