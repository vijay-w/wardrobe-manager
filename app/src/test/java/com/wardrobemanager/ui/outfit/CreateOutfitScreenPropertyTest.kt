package com.wardrobemanager.ui.outfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import com.wardrobemanager.ui.theme.WardrobeManagerTheme
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateOutfitScreenPropertyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `**Feature: wardrobe-manager, Property 7: 衣服选择预览一致性**`() = runTest {
        checkAll(clothingItemsGenerator()) { clothingItems ->
            // Mock repositories
            val mockClothingRepository = mockk<ClothingRepository>()
            val mockOutfitRepository = mockk<OutfitRepository>()
            
            coEvery { mockClothingRepository.getAllClothing() } returns flowOf(clothingItems)
            
            // Create ViewModel with mocked repositories
            val viewModel = CreateOutfitViewModel(mockOutfitRepository, mockClothingRepository)
            
            composeTestRule.setContent {
                WardrobeManagerTheme {
                    CreateOutfitScreen(
                        onNavigateBack = {},
                        onOutfitCreated = {},
                        viewModel = viewModel
                    )
                }
            }
            
            // Wait for content to load
            composeTestRule.waitForIdle()
            
            // Select some clothing items
            val itemsToSelect = clothingItems.take(2) // Select first 2 items
            
            itemsToSelect.forEach { clothingItem ->
                // Click on clothing item to select it
                composeTestRule
                    .onNodeWithText(clothingItem.name)
                    .performClick()
                
                composeTestRule.waitForIdle()
                
                // Verify the item appears in the selected items preview
                // Look for the item in the selected items section
                composeTestRule
                    .onAllNodesWithText(clothingItem.name)
                    .assertCountEquals(2) // Should appear in both available and selected sections
            }
            
            // Verify the selected count is displayed correctly
            if (itemsToSelect.isNotEmpty()) {
                composeTestRule
                    .onNodeWithText("已选择的衣服 (${itemsToSelect.size})")
                    .assertIsDisplayed()
            }
        }
    }

    companion object {
        fun clothingItemsGenerator() = arbitrary { rs ->
            val itemCount = Arb.int(2..4).bind() // Limit to avoid UI test complexity
            (1..itemCount).map { index ->
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
        }
    }
}