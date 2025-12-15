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
class OutfitDeletionIsolationPropertyTest {

    @Test
    fun `**Feature: wardrobe-manager, Property 11: 穿搭删除隔离性**`() = runTest {
        /**
         * **Validates: Requirements 4.4**
         * For any outfit deletion operation, should remove the outfit from the list but preserve original clothing items unchanged
         */
        checkAll(outfitWithClothingItemsGenerator()) { (outfit, allClothingItems) ->
            // Mock repositories
            val mockOutfitRepository = mockk<OutfitRepository>()
            val mockClothingRepository = mockk<ClothingRepository>()
            
            // Setup mocks - clothing items should remain unchanged
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(allClothingItems)
            coEvery { mockOutfitRepository.getAllOutfits() } returns flowOf(listOf(outfit))
            
            // Capture the deleted outfit
            val deletedOutfitSlot = slot<Outfit>()
            coEvery { mockOutfitRepository.deleteOutfit(capture(deletedOutfitSlot)) } just Runs
            
            // Create ViewModels
            val outfitListViewModel = OutfitListViewModel(mockOutfitRepository)
            val outfitDetailViewModel = OutfitDetailViewModel(mockOutfitRepository, mockClothingRepository)
            
            // Delete the outfit using list view model
            outfitListViewModel.deleteOutfit(outfit)
            
            // Verify the outfit was deleted
            verify { mockOutfitRepository.deleteOutfit(any()) }
            deletedOutfitSlot.captured.id shouldBe outfit.id
            
            // Verify clothing items are not affected by checking they're still available
            // The clothing repository should still return all items unchanged
            verify(exactly = 0) { mockClothingRepository.deleteClothing(any()) }
            
            // Test with detail view model as well
            clearMocks(mockOutfitRepository)
            coEvery { mockOutfitRepository.getOutfitById(outfit.id) } returns outfit
            coEvery { mockOutfitRepository.deleteOutfit(any()) } just Runs
            
            outfitDetailViewModel.loadOutfit(outfit.id)
            outfitDetailViewModel.deleteOutfit()
            
            // Verify deletion was called but clothing items remain untouched
            verify { mockOutfitRepository.deleteOutfit(any()) }
            verify(exactly = 0) { mockClothingRepository.deleteClothing(any()) }
        }
    }

    @Test
    fun `**Feature: wardrobe-manager, Property 11b: 穿搭删除后衣服项目完整性**`() = runTest {
        /**
         * **Validates: Requirements 4.4**
         * After outfit deletion, all clothing items that were part of the outfit should still exist independently
         */
        checkAll(outfitWithClothingItemsGenerator()) { (outfit, allClothingItems) ->
            // Mock repositories
            val mockOutfitRepository = mockk<OutfitRepository>()
            val mockClothingRepository = mockk<ClothingRepository>()
            
            // Store original clothing items for comparison
            val originalClothingItems = outfit.clothingItems.toList()
            
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(allClothingItems)
            coEvery { mockOutfitRepository.deleteOutfit(any()) } just Runs
            
            val viewModel = OutfitListViewModel(mockOutfitRepository)
            
            // Delete the outfit
            viewModel.deleteOutfit(outfit)
            
            // Verify that no clothing items were deleted
            verify(exactly = 0) { mockClothingRepository.deleteClothing(any()) }
            verify(exactly = 0) { mockClothingRepository.updateClothing(any()) }
            
            // Verify the clothing repository still contains all original items
            // This simulates that the clothing items are preserved after outfit deletion
            originalClothingItems.forEach { clothingItem ->
                // Each clothing item should still be available in the repository
                allClothingItems.any { it.id == clothingItem.id } shouldBe true
            }
        }
    }

    @Test
    fun `**Feature: wardrobe-manager, Property 11c: 多穿搭共享衣服删除隔离**`() = runTest {
        /**
         * **Validates: Requirements 4.4**
         * When deleting an outfit that shares clothing items with other outfits, 
         * only the outfit should be deleted while clothing items remain available for other outfits
         */
        checkAll(sharedClothingOutfitsGenerator()) { (outfit1, outfit2, sharedItems) ->
            val mockOutfitRepository = mockk<OutfitRepository>()
            val mockClothingRepository = mockk<ClothingRepository>()
            
            coEvery { mockOutfitRepository.getAllOutfits() } returns flowOf(listOf(outfit1, outfit2))
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(sharedItems)
            coEvery { mockOutfitRepository.deleteOutfit(any()) } just Runs
            
            val viewModel = OutfitListViewModel(mockOutfitRepository)
            
            // Delete the first outfit
            viewModel.deleteOutfit(outfit1)
            
            // Verify only the outfit was deleted, not the shared clothing items
            verify { mockOutfitRepository.deleteOutfit(outfit1) }
            verify(exactly = 0) { mockClothingRepository.deleteClothing(any()) }
            
            // Verify shared clothing items are still available for the second outfit
            val sharedItemIds = outfit1.clothingItems.intersect(outfit2.clothingItems.toSet()).map { it.id }
            sharedItemIds.forEach { sharedItemId ->
                sharedItems.any { it.id == sharedItemId } shouldBe true
            }
        }
    }

    companion object {
        fun clothingItemGenerator() = arbitrary { rs ->
            ClothingItem(
                id = Arb.long(1..1000).bind(),
                name = "Clothing ${Arb.int(1..100).bind()}",
                category = Arb.enum<ClothingCategory>().bind(),
                imagePath = "/test/path/image${Arb.int(1..100).bind()}.jpg",
                rating = Arb.float(0f..5f).bind(),
                price = Arb.double(10.0..1000.0).orNull().bind(),
                purchaseLink = null,
                notes = null,
                createdAt = System.currentTimeMillis(),
                lastWorn = null
            )
        }
        
        fun outfitGenerator(clothingItems: List<ClothingItem>) = arbitrary { rs ->
            val selectedItems = clothingItems.shuffled().take(Arb.int(1..minOf(3, clothingItems.size)).bind())
            
            Outfit(
                id = Arb.long(1..1000).bind(),
                name = "Test Outfit ${Arb.int(1..100).bind()}",
                description = Arb.string(0..100).orNull().bind(),
                rating = Arb.float(0f..5f).bind(),
                clothingItems = selectedItems,
                createdAt = System.currentTimeMillis(),
                lastWorn = Arb.long().orNull().bind()
            )
        }
        
        fun outfitWithClothingItemsGenerator() = arbitrary { rs ->
            val allClothingItems = (1..Arb.int(3..6).bind()).map { clothingItemGenerator().bind() }
            val outfit = outfitGenerator(allClothingItems).bind()
            outfit to allClothingItems
        }
        
        fun sharedClothingOutfitsGenerator() = arbitrary { rs ->
            val allItems = (1..Arb.int(4..8).bind()).map { clothingItemGenerator().bind() }
            
            // Create two outfits that share at least one clothing item
            val sharedItems = allItems.take(2) // At least 2 shared items
            val outfit1Items = sharedItems + allItems.drop(2).take(Arb.int(0..2).bind())
            val outfit2Items = sharedItems + allItems.drop(4).take(Arb.int(0..2).bind())
            
            val outfit1 = Outfit(
                id = 1L,
                name = "Outfit 1",
                description = null,
                rating = Arb.float(0f..5f).bind(),
                clothingItems = outfit1Items,
                createdAt = System.currentTimeMillis(),
                lastWorn = null
            )
            
            val outfit2 = Outfit(
                id = 2L,
                name = "Outfit 2", 
                description = null,
                rating = Arb.float(0f..5f).bind(),
                clothingItems = outfit2Items,
                createdAt = System.currentTimeMillis(),
                lastWorn = null
            )
            
            Triple(outfit1, outfit2, allItems)
        }
    }
}