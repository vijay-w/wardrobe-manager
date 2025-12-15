package com.wardrobemanager.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("统计信息") }
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.statistics != null -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OverviewCards(statistics = uiState.statistics!!)
                    }
                    
                    item {
                        CategoryDistributionCard(
                            categoryDistribution = uiState.statistics!!.categoryDistribution
                        )
                    }
                    
                    item {
                        ValueStatisticsCard(statistics = uiState.statistics!!)
                    }
                    
                    item {
                        UsageStatisticsCard(
                            usageFrequency = uiState.statistics!!.usageFrequency
                        )
                    }
                    
                    item {
                        RatingDistributionCard(
                            clothingRating = uiState.statistics!!.ratingDistribution,
                            outfitRating = uiState.statistics!!.outfitRatingDistribution,
                            avgClothingRating = uiState.statistics!!.averageRating,
                            avgOutfitRating = uiState.statistics!!.averageOutfitRating
                        )
                    }
                    
                    if (uiState.insights.isNotEmpty()) {
                        item {
                            InsightsCard(insights = uiState.insights)
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无统计数据")
                }
            }
        }
    }
}

@Composable
private fun OverviewCards(
    statistics: WardrobeStatistics,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            StatCard(
                title = "衣服总数",
                value = statistics.totalClothingItems.toString(),
                icon = Icons.Default.Checkroom,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            StatCard(
                title = "穿搭总数",
                value = statistics.totalOutfits.toString(),
                icon = Icons.Default.Style,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        item {
            StatCard(
                title = "总价值",
                value = "¥${String.format("%.0f", statistics.totalValue)}",
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        item {
            StatCard(
                title = "平均评分",
                value = String.format("%.1f", statistics.averageRating),
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryDistributionCard(
    categoryDistribution: Map<ClothingCategory, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "分类分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            categoryDistribution.forEach { (category, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = count.toFloat() / categoryDistribution.values.maxOrNull()!!.toFloat(),
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueStatisticsCard(
    statistics: WardrobeStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "价值统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总价值",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${String.format("%.2f", statistics.totalValue)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column {
                    Text(
                        text = "平均单价",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${String.format("%.2f", statistics.totalValue / statistics.totalClothingItems)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            if (statistics.categoryValueDistribution.isNotEmpty()) {
                Text(
                    text = "分类价值分布",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                statistics.categoryValueDistribution.forEach { (category, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "¥${String.format("%.0f", value)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageStatisticsCard(
    usageFrequency: UsageFrequency,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "使用统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UsageStatItem(
                    title = "本周穿过",
                    value = "${usageFrequency.itemsWornThisWeek}件衣服"
                )
                
                UsageStatItem(
                    title = "本月穿过",
                    value = "${usageFrequency.itemsWornThisMonth}件衣服"
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UsageStatItem(
                    title = "从未穿过",
                    value = "${usageFrequency.totalItemsNeverWorn}件衣服"
                )
                
                UsageStatItem(
                    title = "穿搭使用",
                    value = "${usageFrequency.outfitsWornThisMonth}个本月"
                )
            }
        }
    }
}

@Composable
private fun UsageStatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RatingDistributionCard(
    clothingRating: Map<Int, Int>,
    outfitRating: Map<Int, Int>,
    avgClothingRating: Float,
    avgOutfitRating: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "评分分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "衣服平均评分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f", avgClothingRating),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "穿搭平均评分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f", avgOutfitRating),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(
    insights: List<WardrobeInsight>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "智能洞察",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            insights.forEach { insight ->
                InsightItem(insight = insight)
            }
        }
    }
}

@Composable
private fun InsightItem(
    insight: WardrobeInsight,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (insight.type) {
                InsightType.CATEGORY_POPULAR -> Icons.Default.TrendingUp
                InsightType.VALUE_AVERAGE -> Icons.Default.AttachMoney
                InsightType.USAGE_LOW -> Icons.Default.Warning
                InsightType.RATING_HIGH -> Icons.Default.Star
                InsightType.TREND_INCREASING -> Icons.Default.ShowChart
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Column {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ViewModel for StatisticsScreen
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository,
    private val outfitRepository: OutfitRepository,
    private val statisticsManager: StatisticsManager
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    clothingRepository.getAllClothing(),
                    outfitRepository.getAllOutfits()
                ) { clothingItems, outfits ->
                    val statistics = statisticsManager.calculateWardrobeStatistics(clothingItems, outfits)
                    val insights = statisticsManager.generateInsights(statistics)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statistics = statistics,
                        insights = insights,
                        error = null
                    )
                }.collect { }
                
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        }
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val statistics: WardrobeStatistics? = null,
    val insights: List<WardrobeInsight> = emptyList(),
    val error: String? = null
)