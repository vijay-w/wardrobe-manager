package com.wardrobemanager.ui.imagepicker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
class ImageProcessorTest : StringSpec({

    lateinit var context: Context
    lateinit var imageProcessor: ImageProcessor

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        imageProcessor = ImageProcessor(context)
    }

    "should save and load image successfully" {
        runTest {
            // Create a test image file
            val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            testBitmap.eraseColor(android.graphics.Color.BLUE)
            
            val testFile = File(context.cacheDir, "test_input.jpg")
            FileOutputStream(testFile).use { out ->
                testBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            val testUri = Uri.fromFile(testFile)
            val outputFile = File(context.cacheDir, "test_output.jpg")
            
            try {
                // Process the image
                val result = imageProcessor.processImage(testUri, outputFile)
                
                // Verify processing was successful
                result.isSuccess shouldBe true
                outputFile.exists() shouldBe true
                outputFile.length() shouldNotBe 0L
                
            } finally {
                // Clean up
                testFile.delete()
                outputFile.delete()
                testBitmap.recycle()
            }
        }
    }

    "should handle invalid image format gracefully" {
        runTest {
            // Create a non-image file
            val testFile = File(context.cacheDir, "test_invalid.txt")
            testFile.writeText("This is not an image")
            
            val testUri = Uri.fromFile(testFile)
            val outputFile = File(context.cacheDir, "test_output.jpg")
            
            try {
                // Process the invalid file
                val result = imageProcessor.processImage(testUri, outputFile)
                
                // Verify processing failed gracefully
                result.isFailure shouldBe true
                
            } finally {
                // Clean up
                testFile.delete()
                outputFile.delete()
            }
        }
    }

    "should get image dimensions correctly" {
        runTest {
            // Create a test image with known dimensions
            val width = 200
            val height = 150
            val testBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            val testFile = File(context.cacheDir, "test_dimensions.jpg")
            FileOutputStream(testFile).use { out ->
                testBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            val testUri = Uri.fromFile(testFile)
            
            try {
                // Get dimensions
                val dimensions = imageProcessor.getImageDimensions(testUri)
                
                // Verify dimensions are correct
                dimensions shouldNotBe null
                dimensions?.first shouldBe width
                dimensions?.second shouldBe height
                
            } finally {
                // Clean up
                testFile.delete()
                testBitmap.recycle()
            }
        }
    }

    "should return null for invalid image dimensions" {
        runTest {
            // Create a non-image file
            val testFile = File(context.cacheDir, "test_invalid_dimensions.txt")
            testFile.writeText("Not an image")
            
            val testUri = Uri.fromFile(testFile)
            
            try {
                // Try to get dimensions
                val dimensions = imageProcessor.getImageDimensions(testUri)
                
                // Verify dimensions are null for invalid file
                dimensions shouldBe null
                
            } finally {
                // Clean up
                testFile.delete()
            }
        }
    }
})