package com.wardrobemanager.ui.purchase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PurchaseInfoManagerPropertyTest : StringSpec({

    lateinit var context: Context
    lateinit var purchaseInfoManager: PurchaseInfoManager

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        purchaseInfoManager = PurchaseInfoManager(context)
    }

    "**Feature: wardrobe-manager, Property 14: 链接验证正确性**" {
        runTest {
            checkAll(linkValidationGenerator()) { (link, shouldBeValid) ->
                val result = purchaseInfoManager.validatePurchaseLink(link)
                
                if (shouldBeValid) {
                    result.shouldBeInstanceOf<LinkValidationResult.Valid>()
                    // Valid links should be normalized (have https:// prefix)
                    if (link.isNotBlank()) {
                        val validResult = result as LinkValidationResult.Valid
                        validResult.link?.startsWith("http") shouldBe true
                    }
                } else {
                    result.shouldBeInstanceOf<LinkValidationResult.Invalid>()
                }
            }
        }
    }

}) {
    companion object {
        fun linkValidationGenerator() = arbitrary { rs ->
            val validLinks = listOf(
                "https://www.example.com",
                "http://example.com",
                "www.example.com",
                "example.com",
                "https://shop.example.com/product/123",
                "https://example.com/path?param=value",
                ""  // Empty link should be valid (optional field)
            )
            
            val invalidLinks = listOf(
                "not-a-url",
                "ftp://example.com",  // Wrong protocol
                "https://",  // Incomplete URL
                "example",  // No domain extension
                "https://example.",  // Invalid domain
                "javascript:alert('xss')",  // Potentially malicious
                "file:///etc/passwd"  // Local file
            )
            
            val allLinks = validLinks + invalidLinks
            val selectedLink = Arb.element(allLinks).bind()
            val isValid = validLinks.contains(selectedLink)
            
            Pair(selectedLink, isValid)
        }
    }
}