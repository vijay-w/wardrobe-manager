package com.wardrobemanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ClothingItem(
    val id: Long = 0,
    val name: String,
    val category: ClothingCategory,
    val imagePath: String,
    val rating: Float = 0f,
    val price: Double? = null,
    val purchaseLink: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastWorn: Long? = null
)