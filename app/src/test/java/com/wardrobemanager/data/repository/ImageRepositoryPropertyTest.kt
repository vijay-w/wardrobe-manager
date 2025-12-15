package com.wardrobemanager.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.wardrobemanager.data.repository.ImageRepositoryImpl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
class ImageRepositoryPropertyTest : StringSpec({

    lateinit var context: Context
    lateinit var imageRepository: ImageRepositoryImpl

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        imageRepository = ImageRepositoryImpl(context)
    }

    "**Feature: wardrobe-manager, Property 1: 图片保存一致性**" {
        runTest {
            checkAll(testImageGenerator()) { testImageData ->
                // Create a test image file
                val testFile = createTestImageFile(context, testImageData)
                
                try {
                    // Save the image
                    val savedImagePath = imageRepository.saveImageFromCamera(testFile)
                    
                    // Verify the image was saved successfully
                    savedImagePath shouldNotBe ""
                    val savedFile = imageRepository.getImageFile(savedImagePath)
                    savedFile shouldNotBe null
                    savedFile?.exists() shouldBe true
                    
                    // Clean up
                    imageRepository.deleteImage(savedImagePath)
                } finally {
                    // Clean up test file
                    testFile.delete()
                }
            }
        }
    }

}) {
    companion object {
        data class TestImageData(
            val width: Int,
            val height: Int,
            val quality: Int
        )
        
        fun testImageGenerator() = arbitrary { rs ->
            TestImageData(
                width = Arb.int(100..2000).bind(),
                height = Arb.int(100..2000).bind(),
                quality = Arb.int(50..100).bind()
            )
        }
        
        private fun createTestImageFile(context: Context, imageData: TestImageData): File {
            val bitmap = Bitmap.createBitmap(
                imageData.width, 
                imageData.height, 
                Bitmap.Config.ARGB_8888
            )
            
            // Fill with a test pattern
            bitmap.eraseColor(android.graphics.Color.BLUE)
            
            val testFile = File(context.cacheDir, "test_image_${System.currentTimeMillis()}.jpg")
            FileOutputStream(testFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, imageData.quality, out)
            }
            
            bitmap.recycle()
            return testFile
        }
    }
}