package com.wardrobemanager.ui.navigation

object WardrobeDestinations {
    const val WARDROBE_ROUTE = "wardrobe"
    const val ADD_CLOTHING_ROUTE = "add_clothing"
    const val CAMERA_ROUTE = "camera"
    const val OUTFIT_LIST_ROUTE = "outfit_list"
    const val CREATE_OUTFIT_ROUTE = "create_outfit"
    const val OUTFIT_DETAIL_ROUTE = "outfit_detail/{$OUTFIT_ID_ARG}"
    const val STATISTICS_ROUTE = "statistics"
    const val BACKUP_ROUTE = "backup"
    
    const val OUTFIT_ID_ARG = "outfitId"
    
    // Deep link URIs
    const val DEEP_LINK_BASE = "wardrobemanager://app"
    const val WARDROBE_DEEP_LINK = "$DEEP_LINK_BASE/wardrobe"
    const val ADD_CLOTHING_DEEP_LINK = "$DEEP_LINK_BASE/add_clothing"
    const val OUTFIT_LIST_DEEP_LINK = "$DEEP_LINK_BASE/outfits"
    const val OUTFIT_DETAIL_DEEP_LINK = "$DEEP_LINK_BASE/outfit/{$OUTFIT_ID_ARG}"
    const val STATISTICS_DEEP_LINK = "$DEEP_LINK_BASE/statistics"
    const val BACKUP_DEEP_LINK = "$DEEP_LINK_BASE/backup"
    
    fun createOutfitDetailRoute(outfitId: Long): String {
        return "outfit_detail/$outfitId"
    }
    
    fun createOutfitDetailDeepLink(outfitId: Long): String {
        return "$DEEP_LINK_BASE/outfit/$outfitId"
    }
}