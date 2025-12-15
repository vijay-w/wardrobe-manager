package com.wardrobemanager.ui.statistics

import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor() {
    
    fun calculateWardrobeStatistics(
        clothingItems: List<ClothingItem>,
        outfits: List<Outfit>
    ): WardrobeStatistics {
        return WardrobeStatistics(
            totalClothingItems = clothingItems.size,
            totalOutfits = outfits.size,
            categoryDistribution = calculateCategoryDistribution(clothingItems),
            ratingDistribution = calculateRatingDistribution(clothingItems),
            outfitRatingDistribution = calculateOutfitRatingDistribution(outfits),
            totalValue = calculateTotalValue(clothingItems),
            averageRating = calculateAverageRating(clothingItems),
            averageOutfitRating = calculateAverageOutfitRating(outfits),
            mostUsedItems = findMostUsedItems(clothingItems),
            leastUsedItems = findLeastUsedItems(clothingItems),
            mostUsedOutfits = findMostUsedOutfits(outfits),
            recentlyAddedItems = findRecentlyAddedItems(clothingItems),
            expensiveItems = findMostExpensiveItems(clothingItems),
            categoryValueDistribution = calculateCategoryValueDistribution(clothingItems),
            monthlyAdditionTrend = calculateMonthlyAdditionTrend(clothingItems),
            usageFrequency = calculateUsageFrequency(clothingItems, outfits)
        )
    }
    
    private fun calculateCategoryDistribution(items: List<ClothingItem>): Map<ClothingCategory, Int> {
        return items.groupBy { it.category }
            .mapValues { it.value.size }
    }
    
    private fun calculateRatingDistribution(items: List<ClothingItem>): Map<Int, Int> {
        return items.groupBy { it.rating.toInt() }
            .mapValues { it.value.size }
    }
    
    private fun calculateOutfitRatingDistribution(outfits: List<Outfit>): Map<Int, Int> {
        return outfits.groupBy { it.rating.toInt() }
            .mapValues { it.value.size }
    }
    
    private fun calculateTotalValue(items: List<ClothingItem>): Double {
        return items.mapNotNull { it.price }.sum()
    }
    
    private fun calculateAverageRating(items: List<ClothingItem>): Float {
        val ratedItems = items.filter { it.rating > 0 }
        return if (ratedItems.isNotEmpty()) {
            ratedItems.map { it.rating }.average().toFloat()
        } else {
            0f
        }
    }
    
    private fun calculateAverageOutfitRating(outfits: List<Outfit>): Float {
        val ratedOutfits = outfits.filter { it.rating > 0 }
        return if (ratedOutfits.isNotEmpty()) {
            ratedOutfits.map { it.rating }.average().toFloat()
        } else {
            0f
        }
    }
    
    private fun findMostUsedItems(items: List<ClothingItem>, limit: Int = 5): List<ClothingItem> {
        return items.filter { it.lastWorn != null }
            .sortedByDescending { it.lastWorn }
            .take(limit)
    }
    
    private fun findLeastUsedItems(items: List<ClothingItem>, limit: Int = 5): List<ClothingItem> {
        val neverWorn = items.filter { it.lastWorn == null }
        val rarelyWorn = items.filter { it.lastWorn != null }
            .sortedBy { it.lastWorn }
        
        return (neverWorn + rarelyWorn).take(limit)
    }
    
    private fun findMostUsedOutfits(outfits: List<Outfit>, limit: Int = 5): List<Outfit> {
        return outfits.filter { it.lastWorn != null }
            .sortedByDescending { it.lastWorn }
            .take(limit)
    }
    
    private fun findRecentlyAddedItems(items: List<ClothingItem>, limit: Int = 5): List<ClothingItem> {
        return items.sortedByDescending { it.createdAt }.take(limit)
    }
    
    private fun findMostExpensiveItems(items: List<ClothingItem>, limit: Int = 5): List<ClothingItem> {
        return items.filter { it.price != null }
            .sortedByDescending { it.price }
            .take(limit)
    }
    
    private fun calculateCategoryValueDistribution(items: List<ClothingItem>): Map<ClothingCategory, Double> {
        return items.filter { it.price != null }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.mapNotNull { it.price }.sum() }
    }
    
    private fun calculateMonthlyAdditionTrend(items: List<ClothingItem>): Map<String, Int> {
        val calendar = java.util.Calendar.getInstance()
        val monthlyData = mutableMapOf<String, Int>()
        
        // Get last 12 months
        repeat(12) { i ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(java.util.Calendar.MONTH, -i)
            val monthKey = "${calendar.get(java.util.Calendar.YEAR)}-${String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)}"
            monthlyData[monthKey] = 0
        }
        
        // Count items added each month
        items.forEach { item ->
            calendar.timeInMillis = item.createdAt
            val monthKey = "${calendar.get(java.util.Calendar.YEAR)}-${String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)}"
            monthlyData[monthKey] = monthlyData.getOrDefault(monthKey, 0) + 1
        }
        
        return monthlyData.toSortedMap()
    }
    
    private fun calculateUsageFrequency(items: List<ClothingItem>, outfits: List<Outfit>): UsageFrequency {
        val currentTime = System.currentTimeMillis()
        val oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
        val oneMonthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)
        
        val itemsWornThisWeek = items.count { item ->
            item.lastWorn != null && item.lastWorn > oneWeekAgo
        }
        
        val itemsWornThisMonth = items.count { item ->
            item.lastWorn != null && item.lastWorn > oneMonthAgo
        }
        
        val outfitsWornThisWeek = outfits.count { outfit ->
            outfit.lastWorn != null && outfit.lastWorn > oneWeekAgo
        }
        
        val outfitsWornThisMonth = outfits.count { outfit ->
            outfit.lastWorn != null && outfit.lastWorn > oneMonthAgo
        }
        
        return UsageFrequency(
            itemsWornThisWeek = itemsWornThisWeek,
            itemsWornThisMonth = itemsWornThisMonth,
            outfitsWornThisWeek = outfitsWornThisWeek,
            outfitsWornThisMonth = outfitsWornThisMonth,
            totalItemsNeverWorn = items.count { it.lastWorn == null },
            totalOutfitsNeverWorn = outfits.count { it.lastWorn == null }
        )
    }
    
    fun generateInsights(statistics: WardrobeStatistics): List<WardrobeInsight> {
        val insights = mutableListOf<WardrobeInsight>()
        
        // Category insights
        val mostPopularCategory = statistics.categoryDistribution.maxByOrNull { it.value }
        mostPopularCategory?.let { (category, count) ->
            insights.add(
                WardrobeInsight(
                    type = InsightType.CATEGORY_POPULAR,
                    title = "最多的分类",
                    description = "${category.displayName}是你衣橱中最多的分类，共有${count}件",
                    value = count.toDouble()
                )
            )
        }
        
        // Value insights
        if (statistics.totalValue > 0) {
            val avgItemValue = statistics.totalValue / statistics.totalClothingItems
            insights.add(
                WardrobeInsight(
                    type = InsightType.VALUE_AVERAGE,
                    title = "平均单价",
                    description = "你的衣服平均单价为¥${String.format("%.2f", avgItemValue)}",
                    value = avgItemValue
                )
            )
        }
        
        // Usage insights
        val neverWornPercentage = (statistics.usageFrequency.totalItemsNeverWorn.toDouble() / statistics.totalClothingItems) * 100
        if (neverWornPercentage > 20) {
            insights.add(
                WardrobeInsight(
                    type = InsightType.USAGE_LOW,
                    title = "利用率提醒",
                    description = "${String.format("%.1f", neverWornPercentage)}%的衣服还没有穿过，考虑搭配一下吧",
                    value = neverWornPercentage
                )
            )
        }
        
        return insights
    }
}

data class WardrobeStatistics(
    val totalClothingItems: Int,
    val totalOutfits: Int,
    val categoryDistribution: Map<ClothingCategory, Int>,
    val ratingDistribution: Map<Int, Int>,
    val outfitRatingDistribution: Map<Int, Int>,
    val totalValue: Double,
    val averageRating: Float,
    val averageOutfitRating: Float,
    val mostUsedItems: List<ClothingItem>,
    val leastUsedItems: List<ClothingItem>,
    val mostUsedOutfits: List<Outfit>,
    val recentlyAddedItems: List<ClothingItem>,
    val expensiveItems: List<ClothingItem>,
    val categoryValueDistribution: Map<ClothingCategory, Double>,
    val monthlyAdditionTrend: Map<String, Int>,
    val usageFrequency: UsageFrequency
)

data class UsageFrequency(
    val itemsWornThisWeek: Int,
    val itemsWornThisMonth: Int,
    val outfitsWornThisWeek: Int,
    val outfitsWornThisMonth: Int,
    val totalItemsNeverWorn: Int,
    val totalOutfitsNeverWorn: Int
)

data class WardrobeInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val value: Double
)

enum class InsightType {
    CATEGORY_POPULAR,
    VALUE_AVERAGE,
    USAGE_LOW,
    RATING_HIGH,
    TREND_INCREASING
}