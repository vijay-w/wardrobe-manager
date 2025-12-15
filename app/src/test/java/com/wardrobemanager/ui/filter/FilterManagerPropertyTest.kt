package com.wardrobemanager.ui.filter

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilterManagerPropertyTest : StringSpec({

    lateinit var filterManager: FilterManager

    beforeTest {
        filterManager = FilterManager()
    }

    "**Feature: wardrobe-manager, Property 13: 筛选重置完整性**" {
        runTest {
            checkAll(filterResetTestDataGenerator()) { (clothingItems, initialFilters) ->
                // Apply initial filters
                val filteredItems = filterManager.applyFilters(clothingItems, initialFilters)
                
                // Reset filters
                val resetFilters = filterManager.resetFilters()
                
                // Apply reset filters (should return all items)
                val itemsAfterReset = filterManager.applyFilters(clothingItems, resetFilters)
                
                // Verify reset filters are empty/default
                resetFilters.categories shouldBe emptySet()
                resetFilters.minRating shouldBe null
                resetFilters.maxRating shouldBe null
                resetFilters.minPrice shouldBe null
                resetFilters.maxPrice shouldBe null
                resetFilters.wornFilter shouldBe WornFilter.ALL
                resetFilters.sortBy shouldBe SortBy.DATE_ADDED
                
                // Verify that after reset, all original items are returned (sorted by date added)
                val expectedItems = clothingItems.sortedByDescending { it.createdAt }
                itemsAfterReset shouldContainExactly expectedItems
            }
        }
    }

}) {
    companion object {
        fun filterResetTestDataGenerator() = arbitrary { rs ->
            val clothingItems = (1..5).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = "Item $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = null,
                    createdAt = System.currentTimeMillis() - (index * 1000), // Different timestamps
                    lastWorn = null
                )
            }
            
            // Create some non-default filters
            val initialFilters = ClothingFilters(
                categories = setOf(Arb.enum<ClothingCategory>().bind()),
                minRating = Arb.float(1f..3f).bind(),
                maxRating = Arb.float(3f..5f).bind(),
                minPrice = Arb.double(50.0..200.0).bind(),
                maxPrice = Arb.double(500.0..1000.0).bind(),
                wornFilter = Arb.enum<WornFilter>().bind(),
                sortBy = Arb.enum<SortBy>().bind()
            )
            
            Pair(clothingItems, initialFilters)
        }
    }
}