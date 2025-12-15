package com.wardrobemanager.ui.wardrobe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WardrobeViewModelPropertyTest : StringSpec({

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var clothingRepository: ClothingRepository
    lateinit var viewModel: WardrobeViewModel

    beforeTest {
        clothingRepository = mockk()
    }

    "**Feature: wardrobe-manager, Property 5: 分类筛选准确性**" {
        runTest {
            checkAll(categoryFilterGenerator()) { (clothingItems, selectedCategory) ->
                // Mock repository to return all items for filtered query
                val expectedFilteredItems = clothingItems.filter { it.category == selectedCategory }
                coEvery { 
                    clothingRepository.getFilteredClothing(
                        category = selectedCategory,
                        minRating = null,
                        maxRating = null,
                        minPrice = null,
                        maxPrice = null,
                        sortByRating = false
                    )
                } returns flowOf(expectedFilteredItems)

                viewModel = WardrobeViewModel(clothingRepository)
                
                // Apply category filter
                viewModel.updateCategory(selectedCategory)
                
                // Verify filtered results contain only items of selected category
                val uiState = viewModel.uiState.value
                uiState.clothingItems.map { it.category } shouldContainOnly listOf(selectedCategory)
                uiState.selectedCategory shouldBe selectedCategory
            }
        }
    }

}) {
    companion object {
        fun clothingItemGenerator() = arbitrary { rs ->
            ClothingItem(
                id = Arb.long(1..1000).bind(),
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
        
        fun categoryFilterGenerator() = arbitrary { rs ->
            val selectedCategory = Arb.enum<ClothingCategory>().bind()
            val itemCount = Arb.int(3..10).bind()
            
            // Generate items with mixed categories, ensuring at least one matches selected
            val clothingItems = (1..itemCount).map { index ->
                if (index <= itemCount / 2) {
                    // Half the items match the selected category
                    clothingItemGenerator().bind().copy(category = selectedCategory)
                } else {
                    // Other half have different categories
                    val otherCategories = ClothingCategory.values().filter { it != selectedCategory }
                    clothingItemGenerator().bind().copy(
                        category = Arb.element(otherCategories).bind()
                    )
                }
            }
            
            Pair(clothingItems, selectedCategory)
        }
    }
}