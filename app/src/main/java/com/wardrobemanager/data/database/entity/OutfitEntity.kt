package com.wardrobemanager.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val lastWorn: Long? = null
)