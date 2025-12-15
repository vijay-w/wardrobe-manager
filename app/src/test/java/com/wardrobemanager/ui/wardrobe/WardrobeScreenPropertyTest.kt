package com.wardrobemanager.ui.wardrobe

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
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
class WardrobeScreenPropertyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `**Feature: wardrobe-manager, Property 3: 衣服显示完整性**`() = runTest {
        checkAll(clothingItemsGenerator()) { clothingItems ->
            // Mock repository
            val mockRepository = mockk<ClothingRepository>()
            coEvery { mockRepository.getAllClothing() } returns flowOf(clothingItems)
            coEvery { 
                mockRepository.getFilteredClothing(any(), any(), any(), any(), any(), any()) 
            } returns flowOf(clothingItems)
            
            // Create ViewModel with mocked repository
            val viewModel = WardrobeViewModel(mockRepository)
            
            composeTestRule.setContent {
                WardrobeManagerTheme {
                    WardrobeScreen(
                        onClothingItemClick = {},
                        onAddClothingClick = {},
                        viewModel = viewModel
                    )
                }
            }
            
            // Wait for content to load
            composeTestRule.waitForIdle()
            
            // Verify each clothing item displays required information
            clothingItems.forEach { clothingItem ->
                // Check that clothing item name is displayed
                composeTestRule
                    .onNodeWithText(clothingItem.name)
                    .assertIsDisplayed()
                
                // Check that category is displayed
                composeTestRule
                    .onNodeWithText(clothingItem.category.displayName)
                    .assertIsDisplayed()
                
                // If price exists, check it's displayed
                clothingItem.price?.let { price ->
                    val priceText = "¥${String.format("%.2f", price)}"
                    composeTestRule
                        .onNodeWithText(priceText)
                        .assertIsDisplayed()
                }
            }
        }
    }

    companion object {
        fun clothingItemsGenerator() = arbitrary { rs ->
            val itemCount = Arb.int(1..5).bind() // Limit to avoid UI test complexity
            (1..itemCount).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "Test Item $index",
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