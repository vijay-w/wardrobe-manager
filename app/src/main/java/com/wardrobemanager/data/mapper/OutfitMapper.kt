package com.wardrobemanager.data.mapper

import com.wardrobemanager.data.database.entity.OutfitEntity
import com.wardrobemanager.data.database.entity.OutfitWithClothingItems
import com.wardrobemanager.data.model.Outfit

fun OutfitEntity.toOutfit(): Outfit {
    return Outfit(
        id = id,
        name = name,
        description = description,
        rating = rating,
        clothingItems = emptyList(), // Will be populated separately
        createdAt = createdAt,
        lastWorn = lastWorn
    )
}

fun OutfitWithClothingItems.toOutfit(): Outfit {
    return Outfit(
        id = outfit.id,
        name = outfit.name,
        description = outfit.description,
        rating = outfit.rating,
        clothingItems = clothingItems.map { it.toClothingItem() },
        createdAt = outfit.createdAt,
        lastWorn = outfit.lastWorn
    )
}

fun Outfit.toOutfitEntity(): OutfitEntity {
    return OutfitEntity(
        id = id,
        name = name,
        description = description,
        rating = rating,
        createdAt = createdAt,
        lastWorn = lastWorn
    )
}