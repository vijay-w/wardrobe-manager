package com.wardrobemanager.data.mapper

import com.wardrobemanager.data.database.entity.ClothingItemEntity
import com.wardrobemanager.data.model.ClothingItem

fun ClothingItemEntity.toClothingItem(): ClothingItem {
    return ClothingItem(
        id = id,
        name = name,
        category = category,
        imagePath = imagePath,
        rating = rating,
        price = price,
        purchaseLink = purchaseLink,
        notes = notes,
        createdAt = createdAt,
        lastWorn = lastWorn
    )
}

fun ClothingItem.toClothingItemEntity(): ClothingItemEntity {
    return ClothingItemEntity(
        id = id,
        name = name,
        category = category,
        imagePath = imagePath,
        rating = rating,
        price = price,
        purchaseLink = purchaseLink,
        notes = notes,
        createdAt = createdAt,
        lastWorn = lastWorn
    )
}