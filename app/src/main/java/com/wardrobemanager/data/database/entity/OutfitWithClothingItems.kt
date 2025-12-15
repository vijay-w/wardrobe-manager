package com.wardrobemanager.data.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class OutfitWithClothingItems(
    @Embedded val outfit: OutfitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            OutfitClothingCrossRef::class,
            parentColumn = "outfitId",
            entityColumn = "clothingItemId"
        )
    )
    val clothingItems: List<ClothingItemEntity>
)