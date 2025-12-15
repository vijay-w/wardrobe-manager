package com.wardrobemanager.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ClothingCategory(val displayName: String) {
    TOP("上衣"),
    BOTTOM("下装"),
    OUTERWEAR("外套"),
    SHOES("鞋子"),
    ACCESSORY("配饰")
}