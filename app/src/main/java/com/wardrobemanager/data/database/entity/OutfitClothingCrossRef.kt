package com.wardrobemanager.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "outfit_clothing_cross_ref",
    primaryKeys = ["outfitId", "clothingItemId"],
    foreignKeys = [
        ForeignKey(
            entity = OutfitEntity::class,
            parentColumns = ["id"],
            childColumns = ["outfitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClothingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["clothingItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["outfitId"]),
        Index(value = ["clothingItemId"])
    ]
)
data class OutfitClothingCrossRef(
    val outfitId: Long,
    val clothingItemId: Long
)