package com.wardrobemanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Outfit(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val rating: Float = 0f,
    val clothingItems: List<ClothingItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastWorn: Long? = null
)