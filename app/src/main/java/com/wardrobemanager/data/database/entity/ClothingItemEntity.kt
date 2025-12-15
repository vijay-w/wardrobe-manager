package com.wardrobemanager.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wardrobemanager.data.model.ClothingCategory

@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true)
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