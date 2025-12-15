package com.wardrobemanager.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wardrobemanager.ui.theme.WardrobeManagerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigationFlow_wardrobeToAddClothing_navigatesCorrectly() {
        composeTestRule.setContent {
            WardrobeManagerTheme {
                val navController = rememberNavController()
                WardrobeNavigation(navController = navController)
            }
        }

        // Verify we start at wardrobe screen
        composeTestRule.onNodeWithText("衣橱").assertIsDisplayed()
        
        // Navigate to add clothing
        composeTestRule.onNodeWithContentDescription("添加衣服").performClick()
        
        // Verify we're now on add clothing screen
        composeTestRule.onNodeWithText("添加衣服").assertIsDisplayed()
    }

    @Test
    fun navigationFlow_addClothingToCamera_navigatesCorrectly() {
        composeTestRule.setContent {
            WardrobeManagerTheme {
                val navController = rememberNavController()
                WardrobeNavigation(navController = navController)
            }
        }

        // Navigate to add clothing first
        composeTestRule.onNodeWithContentDescription("添加衣服").performClick()
        
        // Then navigate to camera
        composeTestRule.onNodeWithText("拍照").performClick()
        
        // Verify we're on camera screen (would need camera permission handling in real test)
        // This is a basic structure test
        composeTestRule.onNodeWithContentDescription("关闭").assertExists()
    }

    @Test
    fun navigationFlow_outfitListToCreateOutfit_navigatesCorrectly() {
        composeTestRule.setContent {
            WardrobeManagerTheme {
                val navController = rememberNavController()
                WardrobeNavigation(navController = navController)
            }
        }

        // Navigate to outfit list via bottom navigation
        composeTestRule.onNodeWithText("穿搭").performClick()
        
        // Verify we're on outfit list screen
        composeTestRule.onNodeWithText("穿搭").assertIsDisplayed()
        
        // Navigate to create outfit
        composeTestRule.onNodeWithContentDescription("创建穿搭").performClick()
        
        // Verify we're on create outfit screen
        composeTestRule.onNodeWithText("创建穿搭").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchesBetweenScreens() {
        composeTestRule.setContent {
            WardrobeManagerTheme {
                val navController = rememberNavController()
                WardrobeNavigation(navController = navController)
            }
        }

        // Start at wardrobe
        composeTestRule.onNodeWithText("衣橱").assertIsDisplayed()
        
        // Navigate to outfits
        composeTestRule.onNodeWithText("穿搭").performClick()
        composeTestRule.onNodeWithText("还没有穿搭").assertIsDisplayed()
        
        // Navigate to statistics
        composeTestRule.onNodeWithText("统计").performClick()
        composeTestRule.onNodeWithText("衣橱统计").assertIsDisplayed()
        
        // Navigate to backup
        composeTestRule.onNodeWithText("备份").performClick()
        composeTestRule.onNodeWithText("数据备份").assertIsDisplayed()
        
        // Navigate back to wardrobe
        composeTestRule.onNodeWithText("衣橱").performClick()
        composeTestRule.onNodeWithText("衣橱是空的").assertIsDisplayed()
    }

    @Test
    fun backNavigation_returnsToCorrectScreen() {
        composeTestRule.setContent {
            WardrobeManagerTheme {
                val navController = rememberNavController()
                WardrobeNavigation(navController = navController)
            }
        }

        // Navigate to add clothing
        composeTestRule.onNodeWithContentDescription("添加衣服").performClick()
        composeTestRule.onNodeWithText("添加衣服").assertIsDisplayed()
        
        // Navigate back
        composeTestRule.onNodeWithContentDescription("返回").performClick()
        
        // Should be back at wardrobe
        composeTestRule.onNodeWithText("衣橱是空的").assertIsDisplayed()
    }

    @Test
    fun deepLinkNavigation_navigatesToCorrectDestination() {
        // This would test deep link navigation in a real scenario
        // For now, we test that the routes are properly defined
        
        val wardrobeRoute = WardrobeDestinations.WARDROBE_ROUTE
        val addClothingRoute = WardrobeDestinations.ADD_CLOTHING_ROUTE
        val outfitListRoute = WardrobeDestinations.OUTFIT_LIST_ROUTE
        val statisticsRoute = WardrobeDestinations.STATISTICS_ROUTE
        val backupRoute = WardrobeDestinations.BACKUP_ROUTE
        
        // Verify routes are not empty
        assert(wardrobeRoute.isNotEmpty())
        assert(addClothingRoute.isNotEmpty())
        assert(outfitListRoute.isNotEmpty())
        assert(statisticsRoute.isNotEmpty())
        assert(backupRoute.isNotEmpty())
        
        // Verify deep link URIs are properly formatted
        assert(WardrobeDestinations.WARDROBE_DEEP_LINK.startsWith("wardrobemanager://app"))
        assert(WardrobeDestinations.ADD_CLOTHING_DEEP_LINK.startsWith("wardrobemanager://app"))
        assert(WardrobeDestinations.OUTFIT_LIST_DEEP_LINK.startsWith("wardrobemanager://app"))
    }
}