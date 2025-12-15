package com.wardrobemanager.ui.statistics

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StatisticsManagerPropertyTest : StringSpec({

    lateinit var statisticsManager: StatisticsManager

    beforeTest {
        statisticsManager = StatisticsManager()
    }

    "**Feature: wardrobe-manager, Property 16: 统计计算准确性**" {
        runTest {
            checkAll(statisticsTestDataGenerator()) { (clothingItems, outfits) ->
                val statistics = statisticsManager.calculateWardrobeStatistics(clothingItems, outfits)
                
                // Verify total counts are accurate
                statistics.totalClothingItems shouldBe clothingItems.size
                statistics.totalOutfits shouldBe outfits.size
                
                // Verify category distribution accuracy
                val expectedCategoryDistribution = clothingItems.groupBy { it.category }
                    .mapValues { it.value.size }
                statistics.categoryDistribution shouldBe expectedCategoryDistribution
                
                // Verify total value calculation
                val expectedTotalValue = clothingItems.mapNotNull { it.price }.sum()
                statistics.totalValue shouldBe expectedTotalValue
                
                // Verify average rating calculation
                val ratedItems = clothingItems.filter { it.rating > 0 }
                val expectedAverageRating = if (ratedItems.isNotEmpty()) {
                    ratedItems.map { it.rating }.average().toFloat()
                } else {
                    0f
                }
                statistics.averageRating shouldBe expectedAverageRating
                
                // Verify outfit average rating calculation
                val ratedOutfits = outfits.filter { it.rating > 0 }
                val expectedOutfitAverageRating = if (ratedOutfits.isNotEmpty()) {
                    ratedOutfits.map { it.rating }.average().toFloat()
                } else {
                    0f
                }
                statistics.averageOutfitRating shouldBe expectedOutfitAverageRating
                
                // Verify category value distribution
                val expectedCategoryValueDistribution = clothingItems.filter { it.price != null }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.mapNotNull { it.price }.sum() }
                statistics.categoryValueDistribution shouldBe expectedCategoryValueDistribution
                
                // Verify usage frequency calculations
                val currentTime = System.currentTimeMillis()
                val oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
                val oneMonthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)
                
                val expectedItemsWornThisWeek = clothingItems.count { item ->
                    item.lastWorn != null && item.lastWorn > oneWeekAgo
                }
                val expectedItemsWornThisMonth = clothingItems.count { item ->
                    item.lastWorn != null && item.lastWorn > oneMonthAgo
                }
                val expectedItemsNeverWorn = clothingItems.count { it.lastWorn == null }
                
                statistics.usageFrequency.itemsWornThisWeek shouldBe expectedItemsWornThisWeek
                statistics.usageFrequency.itemsWornThisMonth shouldBe expectedItemsWornThisMonth
                statistics.usageFrequency.totalItemsNeverWorn shouldBe expectedItemsNeverWorn
            }
        }
    }

}) {
    companion object {
        fun statisticsTestDataGenerator() = arbitrary { rs ->
            val clothingItems = (1..10).map { index ->
                val currentTime = System.currentTimeMillis()
                ClothingItem(
                    id = index.toLong(),
                    name = "Item $index",
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = null,
                    createdAt = currentTime - (index * 24 * 60 * 60 * 1000), // Different days
                    lastWorn = if (Arb.boolean().bind()) {
                        // Some items worn recently, some long ago, some never
                        when (index % 3) {
                            0 -> currentTime - (2 * 24 * 60 * 60 * 1000) // 2 days ago (this week)
                            1 -> currentTime - (15 * 24 * 60 * 60 * 1000) // 15 days ago (this month)
                            else -> null // Never worn
                        }
                    } else null
                )
            }
            
            val outfits = (1..5).map { index ->
                Outfit(
                    id = index.toLong(),
                    name = "Outfit $index",
                    description = "Description $index",
                    rating = Arb.float(0f..5f).bind(),
                    clothingItems = clothingItems.take(2), // Use first 2 items
                    createdAt = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000),
                    lastWorn = if (Arb.boolean().bind()) {
                        System.currentTimeMillis() - (index * 7 * 24 * 60 * 60 * 1000)
                    } else null
                )
            }
            
            Pair(clothingItems, outfits)
        }
    }
}