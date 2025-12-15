package com.wardrobemanager.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wardrobemanager.data.database.WardrobeDatabase
import com.wardrobemanager.data.database.dao.ClothingItemDao
import com.wardrobemanager.data.database.dao.OutfitDao
import com.wardrobemanager.data.mapper.toClothingItemEntity
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OutfitRepositoryPropertyTest : StringSpec({

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var database: WardrobeDatabase
    lateinit var outfitDao: OutfitDao
    lateinit var clothingItemDao: ClothingItemDao
    lateinit var outfitRepository: OutfitRepositoryImpl

    beforeTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WardrobeDatabase::class.java
        ).allowMainThreadQueries().build()
        
        outfitDao = database.outfitDao()
        clothingItemDao = database.clothingItemDao()
        outfitRepository = OutfitRepositoryImpl(outfitDao)
    }

    afterTest {
        database.close()
    }

    "**Feature: wardrobe-manager, Property 8: 穿搭保存完整性**" {
        runTest {
            checkAll(outfitWithClothingGenerator()) { (outfit, clothingItems) ->
                // First, insert the clothing items
                val clothingItemIds = clothingItems.map { clothingItem ->
                    clothingItemDao.insertClothingItem(clothingItem.toClothingItemEntity())
                }
                
                // Update clothing items with their actual IDs
                val updatedClothingItems = clothingItems.mapIndexed { index, item ->
                    item.copy(id = clothingItemIds[index])
                }
                
                // Create outfit with the clothing items
                val outfitWithItems = outfit.copy(clothingItems = updatedClothingItems)
                
                // Save the outfit
                val savedOutfitId = outfitRepository.insertOutfit(outfitWithItems)
                
                // Retrieve the saved outfit
                val retrievedOutfit = outfitRepository.getOutfitById(savedOutfitId)
                
                // Verify all selected clothing items are included
                retrievedOutfit?.clothingItems?.map { it.id } shouldContainAll clothingItemIds
                retrievedOutfit?.clothingItems?.size shouldBe updatedClothingItems.size
            }
        }
    }

}) {
    companion object {
        fun clothingItemGenerator() = arbitrary { rs ->
            ClothingItem(
                id = 0, // Will be set after insertion
                name = Arb.string(5..50).bind(),
                category = Arb.enum<ClothingCategory>().bind(),
                imagePath = Arb.string(10..100).bind(),
                rating = Arb.float(0f..5f).bind(),
                price = Arb.double(0.0..1000.0).orNull().bind(),
                purchaseLink = Arb.string(10..200).orNull().bind(),
                notes = Arb.string(0..500).orNull().bind(),
                createdAt = Arb.long(1000000000000L..System.currentTimeMillis()).bind(),
                lastWorn = Arb.long(1000000000000L..System.currentTimeMillis()).orNull().bind()
            )
        }
        
        fun outfitGenerator() = arbitrary { rs ->
            Outfit(
                id = 0, // Will be set after insertion
                name = Arb.string(5..50).bind(),
                description = Arb.string(0..200).orNull().bind(),
                rating = Arb.float(0f..5f).bind(),
                clothingItems = emptyList(), // Will be set separately
                createdAt = Arb.long(1000000000000L..System.currentTimeMillis()).bind(),
                lastWorn = Arb.long(1000000000000L..System.currentTimeMillis()).orNull().bind()
            )
        }
        
        fun outfitWithClothingGenerator() = arbitrary { rs ->
            val outfit = outfitGenerator().bind()
            val clothingItemCount = Arb.int(1..5).bind() // 1-5 clothing items per outfit
            val clothingItems = (1..clothingItemCount).map { clothingItemGenerator().bind() }
            
            Pair(outfit, clothingItems)
        }
    }
}