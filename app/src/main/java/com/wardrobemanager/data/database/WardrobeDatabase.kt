package com.wardrobemanager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.wardrobemanager.data.database.dao.ClothingItemDao
import com.wardrobemanager.data.database.dao.OutfitDao
import com.wardrobemanager.data.database.entity.ClothingItemEntity
import com.wardrobemanager.data.database.entity.OutfitEntity
import com.wardrobemanager.data.database.entity.OutfitClothingCrossRef

@Database(
    entities = [
        ClothingItemEntity::class,
        OutfitEntity::class,
        OutfitClothingCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WardrobeDatabase : RoomDatabase() {
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun outfitDao(): OutfitDao

    companion object {
        const val DATABASE_NAME = "wardrobe_database"
    }
}