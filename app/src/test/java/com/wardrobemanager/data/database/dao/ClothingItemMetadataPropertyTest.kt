package com.wardrobemanager.data.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wardrobemanager.data.database.WardrobeDatabase
import com.wardrobemanager.data.database.entity.ClothingItemEntity
import com.wardrobemanager.data.model.ClothingCategory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClothingItemMetadataPropertyTest : StringSpec({

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var database: WardrobeDatabase
    lateinit var dao: ClothingItemDao

    beforeTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WardrobeDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.clothingItemDao()
    }

    afterTest {
        database.close()
    }

    "**Feature: wardrobe-manager, Property 4: 元数据更新一致性**" {
        runTest {
            checkAll(metadataUpdateGenerator()) { (originalItem, newRating, newCategory) ->
                // Insert original item
                val insertedId = dao.insertClothingItem(originalItem)
                
                // Update metadata
                val updatedItem = originalItem.copy(
                    id = insertedId,
                    rating = newRating,
                    category = newCategory
                )
                dao.updateClothingItem(updatedItem)
                
                // Verify metadata was updated correctly
                val retrievedItem = dao.getClothingItemById(insertedId)
                
                retrievedItem?.rating shouldBe newRating
                retrievedItem?.category shouldBe newCategory
                // Other fields should remain unchanged
                retrievedItem?.name shouldBe originalItem.name
                retrievedItem?.imagePath shouldBe originalItem.imagePath
            }
        }
    }

}) {
    companion object {
        fun metadataUpdateGenerator() = arbitrary { rs ->
            val originalItem = ClothingItemEntity(
                id = 0,
                name = Arb.string(5..50).bind(),
                category = Arb.enum<ClothingCategory>().bind(),
                imagePath = Arb.string(10..100).bind(),
                rating = Arb.float(0f..5f).bind(),
                price = Arb.double(0.0..10000.0).orNull().bind(),
                purchaseLink = Arb.string(10..200).orNull().bind(),
                notes = Arb.string(0..500).orNull().bind(),
                createdAt = Arb.long(1000000000000L..System.currentTimeMillis()).bind(),
                lastWorn = Arb.long(1000000000000L..System.currentTimeMillis()).orNull().bind()
            )
            
            val newRating = Arb.float(0f..5f).bind()
            val newCategory = Arb.enum<ClothingCategory>().bind()
            
            Triple(originalItem, newRating, newCategory)
        }
    }
}