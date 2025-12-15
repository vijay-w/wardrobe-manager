package com.wardrobemanager.ui.outfit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OutfitRatingSavePropertyTest {

    @Test
    fun `**Feature: wardrobe-manager, Property 10: 穿搭评分保存一致性**`() = runTest {
        /**
         * **Validates: Requirements 4.2**
         * For any valid outfit rating update, the system should correctly save and update the outfit record
         */
        checkAll(outfitWithRatingGenerator()) { (outfit, newRating) ->
            // Mock repositories
            val mockOutfitRepository = mockk<OutfitRepository>()
            val mockClothingRepository = mockk<ClothingRepository>()
            
            // Setup mocks
            coEvery { mockOutfitRepository.getOutfitById(outfit.id) } returns outfit
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(emptyList())
            
            // Capture the updated outfit
            val updatedOutfitSlot = slot<Outfit>()
            coEvery { mockOutfitRepository.updateOutfit(capture(updatedOutfitSlot)) } just Runs
            
            // Create ViewModel
            val viewModel = OutfitDetailViewModel(mockOutfitRepository, mockClothingRepository)
            
            // Load the outfit
            viewModel.loadOutfit(outfit.id)
            
            // Update the rating
            viewModel.updateOutfitRating(newRating)
            
            // Save the outfit
            viewModel.saveOutfit()
            
            // Verify the outfit was updated with the correct rating
            verify { mockOutfitRepository.updateOutfit(any()) }
            
            // Check that the captured outfit has the correct rating
            updatedOutfitSlot.captured.rating shouldBe newRating
            
            // Verify other properties remain unchanged
            updatedOutfitSlot.captured.id shouldBe outfit.id
            updatedOutfitSlot.captured.name shouldBe outfit.name
            updatedOutfitSlot.captured.description shouldBe outfit.description
            updatedOutfitSlot.captured.clothingItems shouldBe outfit.clothingItems
            updatedOutfitSlot.captured.createdAt shouldBe outfit.createdAt
        }
    }

    @Test
    fun `**Feature: wardrobe-manager, Property 10b: 穿搭评分范围验证**`() = runTest {
        /**
         * **Validates: Requirements 4.2**
         * For any outfit rating update, the rating should be within valid range (0-5)
         */
        checkAll(outfitGenerator(), Arb.float(0f..5f)) { outfit, validRating ->
            // Mock repositories
            val mockOutfitRepository = mockk<OutfitRepository>()
            val mockClothingRepository = mockk<ClothingRepository>()
            
            coEvery { mockOutfitRepository.getOutfitById(outfit.id) } returns outfit
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(emptyList())
            
            val updatedOutfitSlot = slot<Outfit>()
            coEvery { mockOutfitRepository.updateOutfit(capture(updatedOutfitSlot)) } just Runs
            
            val viewModel = OutfitDetailViewModel(mockOutfitRepository, mockClothingRepository)
            
            viewModel.loadOutfit(outfit.id)
            viewModel.updateOutfitRating(validRating)
            viewModel.saveOutfit()
            
            // Verify the rating is within valid range
            val savedRating = updatedOutfitSlot.captured.rating
            (savedRating >= 0f && savedRating <= 5f) shouldBe true
            savedRating shouldBe validRating
        }
    }

    companion object {
        fun outfitGenerator() = arbitrary { rs ->
            val clothingItems = (1..Arb.int(0..3).bind()).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "Clothing $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = null,
                    createdAt = System.currentTimeMillis(),
                    lastWorn = null
                )
            }
            
            Outfit(
                id = Arb.long(1..1000).bind(),
                name = "Test Outfit ${Arb.int(1..100).bind()}",
                description = Arb.string(0..100).orNull().bind(),
                rating = Arb.float(0f..5f).bind(),
                clothingItems = clothingItems,
                createdAt = System.currentTimeMillis(),
                lastWorn = Arb.long().orNull().bind()
            )
        }
        
        fun outfitWithRatingGenerator() = arbitrary { rs ->
            val outfit = outfitGenerator().bind()
            val newRating = Arb.float(0f..5f).bind()
            outfit to newRating
        }
    }
}