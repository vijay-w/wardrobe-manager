package com.wardrobemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import com.wardrobemanager.ui.wardrobe.WardrobeScreen
import com.wardrobemanager.ui.addclothing.AddClothingScreen
import com.wardrobemanager.ui.outfit.OutfitListScreen
import com.wardrobemanager.ui.outfit.CreateOutfitScreen
import com.wardrobemanager.ui.outfit.OutfitDetailScreen
import com.wardrobemanager.ui.statistics.StatisticsScreen
import com.wardrobemanager.ui.backup.BackupScreen
import com.wardrobemanager.ui.camera.CameraScreen

@Composable
fun WardrobeNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = WardrobeDestinations.WARDROBE_ROUTE
    ) {
        composable(
            route = WardrobeDestinations.WARDROBE_ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.WARDROBE_DEEP_LINK }
            )
        ) {
            WardrobeScreen(
                onNavigateToAddClothing = {
                    navController.navigate(WardrobeDestinations.ADD_CLOTHING_ROUTE)
                },
                onNavigateToCamera = {
                    navController.navigate(WardrobeDestinations.CAMERA_ROUTE)
                }
            )
        }
        
        composable(
            route = WardrobeDestinations.ADD_CLOTHING_ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.ADD_CLOTHING_DEEP_LINK }
            )
        ) { backStackEntry ->
            AddClothingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate(WardrobeDestinations.CAMERA_ROUTE)
                },
                savedStateHandle = backStackEntry.savedStateHandle
            )
        }
        
        composable(WardrobeDestinations.CAMERA_ROUTE) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onImageCaptured = { imagePath ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavigationKeys.CAPTURED_IMAGE_PATH, imagePath)
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = WardrobeDestinations.OUTFIT_LIST_ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.OUTFIT_LIST_DEEP_LINK }
            )
        ) {
            OutfitListScreen(
                onNavigateToCreateOutfit = {
                    navController.navigate(WardrobeDestinations.CREATE_OUTFIT_ROUTE)
                },
                onNavigateToOutfitDetail = { outfitId ->
                    navController.navigate(
                        WardrobeDestinations.createOutfitDetailRoute(outfitId)
                    )
                }
            )
        }
        
        composable(WardrobeDestinations.CREATE_OUTFIT_ROUTE) {
            CreateOutfitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = WardrobeDestinations.OUTFIT_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(WardrobeDestinations.OUTFIT_ID_ARG) {
                    type = NavType.LongType
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.OUTFIT_DETAIL_DEEP_LINK }
            )
        ) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getLong(WardrobeDestinations.OUTFIT_ID_ARG) ?: 0L
            OutfitDetailScreen(
                outfitId = outfitId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = WardrobeDestinations.STATISTICS_ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.STATISTICS_DEEP_LINK }
            )
        ) {
            StatisticsScreen()
        }
        
        composable(
            route = WardrobeDestinations.BACKUP_ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = WardrobeDestinations.BACKUP_DEEP_LINK }
            )
        ) {
            BackupScreen()
        }
    }
}