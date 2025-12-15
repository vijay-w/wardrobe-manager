package com.wardrobemanager.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data classes for passing complex data between navigation destinations
 */

@Parcelize
data class ClothingItemArgs(
    val id: Long,
    val name: String,
    val imagePath: String
) : Parcelable

@Parcelize
data class OutfitArgs(
    val id: Long,
    val name: String,
    val clothingItemIds: List<Long>
) : Parcelable

/**
 * Navigation parameter keys
 */
object NavigationKeys {
    const val CLOTHING_ITEM = "clothing_item"
    const val OUTFIT = "outfit"
    const val CAPTURED_IMAGE_PATH = "captured_image_path"
    const val SELECTED_CLOTHING_ITEMS = "selected_clothing_items"
    const val OUTFIT_CREATED = "outfit_created"
    const val CLOTHING_SAVED = "clothing_saved"
}

/**
 * Navigation result keys for handling results from other screens
 */
object NavigationResults {
    const val IMAGE_CAPTURED = "image_captured"
    const val CLOTHING_SELECTED = "clothing_selected"
    const val OUTFIT_SAVED = "outfit_saved"
    const val BACKUP_COMPLETED = "backup_completed"
}