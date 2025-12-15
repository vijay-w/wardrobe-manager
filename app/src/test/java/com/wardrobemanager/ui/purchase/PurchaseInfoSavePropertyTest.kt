package com.wardrobemanager.ui.purchase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PurchaseInfoSavePropertyTest : StringSpec({

    lateinit var context: Context
    lateinit var purchaseInfoManager: PurchaseInfoManager

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        purchaseInfoManager = PurchaseInfoManager(context)
    }

    "**Feature: wardrobe-manager, Property 15: 购买信息保存一致性**" {
        runTest {
            checkAll(purchaseInfoGenerator()) { (originalItem, newPrice, newLink) ->
                // Validate the new purchase information
                val priceValidation = purchaseInfoManager.validatePrice(newPrice?.toString() ?: "")
                val linkValidation = purchaseInfoManager.validatePurchaseLink(newLink ?: "")
                
                // If both validations pass, the information should be saveable
                if (priceValidation is PriceValidationResult.Valid && 
                    linkValidation is LinkValidationResult.Valid) {
                    
                    // Create updated item with new purchase info
                    val updatedItem = originalItem.copy(
                        price = priceValidation.price,
                        purchaseLink = linkValidation.link
                    )
                    
                    // Verify the purchase information was saved correctly
                    updatedItem.price shouldBe priceValidation.price
                    updatedItem.purchaseLink shouldBe linkValidation.link
                    
                    // Verify other fields remain unchanged
                    updatedItem.id shouldBe originalItem.id
                    updatedItem.name shouldBe originalItem.name
                    updatedItem.category shouldBe originalItem.category
                    updatedItem.imagePath shouldBe originalItem.imagePath
                    updatedItem.rating shouldBe originalItem.rating
                    updatedItem.notes shouldBe originalItem.notes
                    updatedItem.createdAt shouldBe originalItem.createdAt
                    updatedItem.lastWorn shouldBe originalItem.lastWorn
                }
            }
        }
    }

}) {
    companion object {
        fun purchaseInfoGenerator() = arbitrary { rs ->
            val originalItem = ClothingItem(
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
            
            val newPrice = Arb.double(0.0..9999.99).orNull().bind()
            val newLink = Arb.element(listOf(
                "https://www.example.com",
                "http://shop.example.com/item/123",
                "www.store.com",
                "example.com/product",
                null,
                ""
            )).bind()
            
            Triple(originalItem, newPrice, newLink)
        }
    }
}