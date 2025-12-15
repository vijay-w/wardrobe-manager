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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClothingItemDaoPropertyTest : StringSpec({

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

    "**Feature: wardrobe-manager, Property 2: 衣橱列表完整性**" {
        runTest {
            checkAll(clothingItemGenerator()) { clothingItem ->
                // Insert clothing item
                val insertedId = dao.insertClothingItem(clothingItem)
                
                // Verify it appears in the wardrobe list
                val allItems = dao.getAllClothingItems().first()
                val insertedItem = allItems.find { it.id == insertedId }
                
                insertedItem shouldBe clothingItem.copy(id = insertedId)
            }
        }
    }

}) {
    companion object {
        fun clothingItemGenerator() = arbitrary { rs ->
            ClothingItemEntity(
                id = 0, // Will be auto-generated
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
        }
    }
}