package com.wardrobemanager.data.database

import androidx.room.TypeConverter
import com.wardrobemanager.data.model.ClothingCategory

class Converters {
    @TypeConverter
    fun fromClothingCategory(category: ClothingCategory): String {
        return category.name
    }

    @TypeConverter
    fun toClothingCategory(categoryName: String): ClothingCategory {
        return ClothingCategory.valueOf(categoryName)
    }
}