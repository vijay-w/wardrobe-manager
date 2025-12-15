package com.wardrobemanager.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardrobemanager.data.model.ClothingCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingFilterBottomSheet(
    filters: ClothingFilters,
    onFiltersChange: (ClothingFilters) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentFilters by remember { mutableStateOf(filters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "筛选选项",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭"
                    )
                }
            }
            
            // Categories
            FilterSection(title = "分类") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ClothingCategory.values()) { category ->
                        FilterChip(
                            selected = currentFilters.categories.contains(category),
                            onClick = {
                                currentFilters = if (currentFilters.categories.contains(category)) {
                                    currentFilters.copy(
                                        categories = currentFilters.categories - category
                                    )
                                } else {
                                    currentFilters.copy(
                                        categories = currentFilters.categories + category
                                    )
                                }
                            },
                            label = { Text(category.displayName) }
                        )
                    }
                }
            }
            
            // Rating Range
            FilterSection(title = "评分范围") {
                RatingRangeSlider(
                    minRating = currentFilters.minRating,
                    maxRating = currentFilters.maxRating,
                    onRatingRangeChange = { min, max ->
                        currentFilters = currentFilters.copy(
                            minRating = min,
                            maxRating = max
                        )
                    }
                )
            }
            
            // Price Range
            FilterSection(title = "价格范围") {
                PriceRangeSlider(
                    minPrice = currentFilters.minPrice,
                    maxPrice = currentFilters.maxPrice,
                    onPriceRangeChange = { min, max ->
                        currentFilters = currentFilters.copy(
                            minPrice = min,
                            maxPrice = max
                        )
                    }
                )
            }
            
            // Worn Filter
            FilterSection(title = "穿着状态") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WornFilter.values().forEach { wornFilter ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentFilters.wornFilter == wornFilter,
                                onClick = {
                                    currentFilters = currentFilters.copy(wornFilter = wornFilter)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (wornFilter) {
                                    WornFilter.ALL -> "全部"
                                    WornFilter.RECENTLY_WORN -> "最近穿过"
                                    WornFilter.NOT_RECENTLY_WORN -> "很久未穿"
                                }
                            )
                        }
                    }
                }
            }
            
            // Sort By
            FilterSection(title = "排序方式") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortBy.values().forEach { sortBy ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentFilters.sortBy == sortBy,
                                onClick = {
                                    currentFilters = currentFilters.copy(sortBy = sortBy)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (sortBy) {
                                    SortBy.DATE_ADDED -> "添加时间"
                                    SortBy.NAME -> "名称"
                                    SortBy.RATING -> "评分"
                                    SortBy.PRICE_LOW_TO_HIGH -> "价格从低到高"
                                    SortBy.PRICE_HIGH_TO_LOW -> "价格从高到低"
                                    SortBy.LAST_WORN -> "最后穿着时间"
                                }
                            )
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        currentFilters = ClothingFilters()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
                
                Button(
                    onClick = {
                        onFiltersChange(currentFilters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("应用")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@Composable
private fun RatingRangeSlider(
    minRating: Float?,
    maxRating: Float?,
    onRatingRangeChange: (Float?, Float?) -> Unit
) {
    var sliderRange by remember {
        mutableStateOf((minRating ?: 0f)..(maxRating ?: 5f))
    }
    
    Column {
        Text(
            text = "评分: ${sliderRange.start.toInt()} - ${sliderRange.endInclusive.toInt()} 星",
            style = MaterialTheme.typography.bodyMedium
        )
        
        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                onRatingRangeChange(
                    if (range.start > 0f) range.start else null,
                    if (range.endInclusive < 5f) range.endInclusive else null
                )
            },
            valueRange = 0f..5f,
            steps = 4
        )
    }
}

@Composable
private fun PriceRangeSlider(
    minPrice: Double?,
    maxPrice: Double?,
    onPriceRangeChange: (Double?, Double?) -> Unit
) {
    var sliderRange by remember {
        mutableStateOf((minPrice?.toFloat() ?: 0f)..(maxPrice?.toFloat() ?: 10000f))
    }
    
    Column {
        Text(
            text = "价格: ¥${sliderRange.start.toInt()} - ¥${sliderRange.endInclusive.toInt()}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                onPriceRangeChange(
                    if (range.start > 0f) range.start.toDouble() else null,
                    if (range.endInclusive < 10000f) range.endInclusive.toDouble() else null
                )
            },
            valueRange = 0f..10000f,
            steps = 99
        )
    }
}