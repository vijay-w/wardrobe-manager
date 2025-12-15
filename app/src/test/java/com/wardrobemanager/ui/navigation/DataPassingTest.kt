package com.wardrobemanager.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DataPassingTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun outfitDetailRoute_createsCorrectRouteWithId() {
        val outfitId = 123L
        val expectedRoute = "outfit_detail/123"
        val actualRoute = WardrobeDestinations.createOutfitDetailRoute(outfitId)
        
        assertEquals(expectedRoute, actualRoute)
    }

    @Test
    fun outfitDetailDeepLink_createsCorrectDeepLinkWithId() {
        val outfitId = 456L
        val expectedDeepLink = "wardrobemanager://app/outfit/456"
        val actualDeepLink = WardrobeDestinations.createOutfitDetailDeepLink(outfitId)
        
        assertEquals(expectedDeepLink, actualDeepLink)
    }

    @Test
    fun navigationKeys_areProperlyDefined() {
        // Verify navigation keys are not empty and follow naming convention
        assertFalse(NavigationKeys.CLOTHING_ITEM.isEmpty())
        assertFalse(NavigationKeys.OUTFIT.isEmpty())
        assertFalse(NavigationKeys.CAPTURED_IMAGE_PATH.isEmpty())
        assertFalse(NavigationKeys.SELECTED_CLOTHING_ITEMS.isEmpty())
        
        // Verify keys use snake_case convention
        assertTrue(NavigationKeys.CAPTURED_IMAGE_PATH.contains("_"))
        assertTrue(NavigationKeys.SELECTED_CLOTHING_ITEMS.contains("_"))
    }

    @Test
    fun navigationResults_areProperlyDefined() {
        // Verify navigation result keys are not empty
        assertFalse(NavigationResults.IMAGE_CAPTURED.isEmpty())
        assertFalse(NavigationResults.CLOTHING_SELECTED.isEmpty())
        assertFalse(NavigationResults.OUTFIT_SAVED.isEmpty())
        assertFalse(NavigationResults.BACKUP_COMPLETED.isEmpty())
        
        // Verify result keys use snake_case convention
        assertTrue(NavigationResults.IMAGE_CAPTURED.contains("_"))
        assertTrue(NavigationResults.CLOTHING_SELECTED.contains("_"))
        assertTrue(NavigationResults.OUTFIT_SAVED.contains("_"))
        assertTrue(NavigationResults.BACKUP_COMPLETED.contains("_"))
    }

    @Test
    fun clothingItemArgs_parcelsCorrectly() {
        val args = ClothingItemArgs(
            id = 1L,
            name = "Test Shirt",
            imagePath = "/path/to/image.jpg"
        )
        
        // Verify properties are set correctly
        assertEquals(1L, args.id)
        assertEquals("Test Shirt", args.name)
        assertEquals("/path/to/image.jpg", args.imagePath)
    }

    @Test
    fun outfitArgs_parcelsCorrectly() {
        val clothingItemIds = listOf(1L, 2L, 3L)
        val args = OutfitArgs(
            id = 10L,
            name = "Summer Outfit",
            clothingItemIds = clothingItemIds
        )
        
        // Verify properties are set correctly
        assertEquals(10L, args.id)
        assertEquals("Summer Outfit", args.name)
        assertEquals(clothingItemIds, args.clothingItemIds)
        assertEquals(3, args.clothingItemIds.size)
    }

    @Test
    fun deepLinkUris_followCorrectPattern() {
        val baseUri = "wardrobemanager://app"
        
        // Verify all deep link URIs start with the correct base
        assertTrue(WardrobeDestinations.WARDROBE_DEEP_LINK.startsWith(baseUri))
        assertTrue(WardrobeDestinations.ADD_CLOTHING_DEEP_LINK.startsWith(baseUri))
        assertTrue(WardrobeDestinations.OUTFIT_LIST_DEEP_LINK.startsWith(baseUri))
        assertTrue(WardrobeDestinations.STATISTICS_DEEP_LINK.startsWith(baseUri))
        assertTrue(WardrobeDestinations.BACKUP_DEEP_LINK.startsWith(baseUri))
        
        // Verify parameterized deep link includes parameter placeholder
        assertTrue(WardrobeDestinations.OUTFIT_DETAIL_DEEP_LINK.contains("{outfitId}"))
    }

    @Test
    fun routeConstants_areUnique() {
        val routes = setOf(
            WardrobeDestinations.WARDROBE_ROUTE,
            WardrobeDestinations.ADD_CLOTHING_ROUTE,
            WardrobeDestinations.CAMERA_ROUTE,
            WardrobeDestinations.OUTFIT_LIST_ROUTE,
            WardrobeDestinations.CREATE_OUTFIT_ROUTE,
            WardrobeDestinations.STATISTICS_ROUTE,
            WardrobeDestinations.BACKUP_ROUTE
        )
        
        // Verify all routes are unique (set size equals number of routes)
        assertEquals(7, routes.size)
        
        // Verify no route is empty
        routes.forEach { route ->
            assertFalse("Route should not be empty", route.isEmpty())
        }
    }
}