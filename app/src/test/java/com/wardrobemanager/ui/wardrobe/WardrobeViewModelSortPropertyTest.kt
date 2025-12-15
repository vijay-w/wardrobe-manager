package com.wardrobemanager.ui.wardrobe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
import io.kotest.core.spec.style.StringSpec
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
class WardrobeViewModelSortPropertyTest : StringSpec({

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var clothingRepository: ClothingRepository
    lateinit var viewModel: WardrobeViewModel

    beforeTest {
        clothingRepository = mockk()
    }

    "**Feature: wardrobe-manager, Property 6: 评分排序正确性**" {
        runTest {
            checkAll(clothingItemsWithRatingsGenerator()) { clothingItems ->
                // Sort items by rating (descending) as expected
                val expectedSortedItems = clothingItems.sortedByDescending { it.rating }
                
                // Mock repository to return sorted items when sortByRating is true
                coEvery { 
                    clothingRepository.getFilteredClothing(
                        category = null,
                        minRating = null,
                        maxRating = null,
                        minPrice = null,
                        maxPrice = null,
                        sortByRating = true
                    )
                } returns flowOf(expectedSortedItems)

                viewModel = WardrobeViewModel(clothingRepository)
                
                // Enable sort by rating
                viewModel.toggleSortByRating()
                
                // Verify items are sorted by rating in descending order
                val uiState = viewModel.uiState.value
                val actualRatings = uiState.clothingItems.map { it.rating }
                val expectedRatings = expectedSortedItems.map { it.rating }
                
                actualRatings shouldBe expectedRatings
                uiState.sortByRating shouldBe true
                
                // Verify the list is actually sorted (each rating >= next rating)
                for (i in 0 until actualRatings.size - 1) {
                    val currentRating = actualRatings[i]
                    val nextRating = actualRatings[i + 1]
                    assert(currentRating >= nextRating) {
                        "Rating at index $i ($currentRating) should be >= rating at index ${i + 1} ($nextRating)"
                    }
                }
            }
        }
    }

}) {
    companion object {
        fun clothingItemsWithRatingsGenerator() = arbitrary { rs ->
            val itemCount = Arb.int(3..8).bind()
            (1..itemCount).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "Item $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "path_$index",
                    rating = Arb.float(0f..5f).bind(), // Different ratings for sorting
                    price = Arb.double(0.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = null,
                    createdAt = System.currentTimeMillis() - (index * 1000), // Different timestamps
                    lastWorn = null
                )
            }
        }
    }
}