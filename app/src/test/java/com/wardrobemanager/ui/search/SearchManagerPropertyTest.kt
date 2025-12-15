package com.wardrobemanager.ui.search

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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchManagerPropertyTest : StringSpec({

    lateinit var searchManager: SearchManager

    beforeTest {
        searchManager = SearchManager()
    }

    "**Feature: wardrobe-manager, Property 12: 搜索结果准确性**" {
        runTest {
            checkAll(searchTestDataGenerator()) { (clothingItems, outfits, searchQuery) ->
                // Filter clothing items using search manager
                val filteredClothingItems = searchManager.filterClothingItems(clothingItems, searchQuery)
                
                // Filter outfits using search manager
                val filteredOutfits = searchManager.filterOutfits(outfits, searchQuery)
                
                // Verify all returned clothing items match the search query
                val lowerQuery = searchQuery.lowercase()
                filteredClothingItems.forEach { item ->
                    val matchesName = item.name.lowercase().contains(lowerQuery)
                    val matchesCategory = item.category.displayName.lowercase().contains(lowerQuery)
                    val matchesNotes = item.notes?.lowercase()?.contains(lowerQuery) == true
                    
                    (matchesName || matchesCategory || matchesNotes) shouldBe true
                }
                
                // Verify all returned outfits match the search query
                filteredOutfits.forEach { outfit ->
                    val matchesName = outfit.name.lowercase().contains(lowerQuery)
                    val matchesDescription = outfit.description?.lowercase()?.contains(lowerQuery) == true
                    val matchesClothingItem = outfit.clothingItems.any { item ->
                        item.name.lowercase().contains(lowerQuery)
                    }
                    
                    (matchesName || matchesDescription || matchesClothingItem) shouldBe true
                }
                
                // Verify that all items that should match are included
                val expectedClothingItems = clothingItems.filter { item ->
                    item.name.lowercase().contains(lowerQuery) ||
                    item.category.displayName.lowercase().contains(lowerQuery) ||
                    item.notes?.lowercase()?.contains(lowerQuery) == true
                }
                
                filteredClothingItems shouldContainAll expectedClothingItems
            }
        }
    }

}) {
    companion object {
        fun searchTestDataGenerator() = arbitrary { rs ->
            val clothingItems = (1..5).map { index ->
                ClothingItem(
                    id = index.toLong(),
                    name = Arb.element(listOf("红色上衣", "蓝色牛仔裤", "白色运动鞋", "黑色外套", "花色裙子")).bind(),
                    category = Arb.enum<ClothingCategory>().bind(),
                    imagePath = "/test/path/image$index.jpg",
                    rating = Arb.float(0f..5f).bind(),
                    price = Arb.double(10.0..1000.0).orNull().bind(),
                    purchaseLink = null,
                    notes = Arb.element(listOf("舒适", "时尚", "经典", null)).bind(),
                    createdAt = System.currentTimeMillis(),
                    lastWorn = null
                )
            }
            
            val outfits = (1..3).map { index ->
                Outfit(
                    id = index.toLong(),
                    name = Arb.element(listOf("休闲穿搭", "正式装扮", "运动套装")).bind(),
                    description = Arb.element(listOf("适合日常", "商务场合", "健身时穿", null)).bind(),
                    rating = Arb.float(0f..5f).bind(),
                    clothingItems = clothingItems.take(2), // Use first 2 clothing items
                    createdAt = System.currentTimeMillis(),
                    lastWorn = null
                )
            }
            
            val searchQuery = Arb.element(listOf("红色", "上衣", "休闲", "舒适", "牛仔")).bind()
            
            Triple(clothingItems, outfits, searchQuery)
        }
    }
}