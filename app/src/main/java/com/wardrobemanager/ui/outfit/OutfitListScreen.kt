package com.wardrobemanager.ui.outfit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wardrobemanager.R
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.ui.components.RatingBar
import com.wardrobemanager.ui.components.ReadOnlyRatingBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitListScreen(
    onNavigateToCreateOutfit: () -> Unit = {},
    onNavigateToOutfitDetail: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OutfitListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text(stringResource(R.string.nav_outfits)) },
            actions = {
                IconButton(onClick = onNavigateToCreateOutfit) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "创建穿搭"
                    )
                }
            }
        )
        
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("搜索穿搭...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除搜索"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Filter Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.sortByRating,
                    onClick = viewModel::toggleSortByRating,
                    label = { Text("按评分排序") }
                )
                
                var showRatingFilter by remember { mutableStateOf(false) }
                FilterChip(
                    selected = uiState.minRating != null || uiState.maxRating != null,
                    onClick = { showRatingFilter = true },
                    label = { Text("评分筛选") }
                )
                
                TextButton(onClick = viewModel::clearFilters) {
                    Text("清除筛选")
                }
                
                // Rating Filter Dialog
                if (showRatingFilter) {
                    RatingFilterDialog(
                        currentMinRating = uiState.minRating,
                        currentMaxRating = uiState.maxRating,
                        onRatingFilterChange = { min, max ->
                            viewModel.updateRatingFilter(min, max)
                            showRatingFilter = false
                        },
                        onDismiss = { showRatingFilter = false }
                    )
                }
            }
            
            // Rating filter
            if (uiState.minRating != null || uiState.maxRating != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "评分筛选:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${uiState.minRating?.toInt() ?: 1} - ${uiState.maxRating?.toInt() ?: 5} 星",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { viewModel.updateRatingFilter(null, null) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除评分筛选",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.outfits.isEmpty() -> {
                    EmptyOutfitContent(
                        onCreateOutfitClick = onNavigateToCreateOutfit,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.outfits) { outfit ->
                            OutfitCard(
                                outfit = outfit,
                                onClick = { onNavigateToOutfitDetail(outfit.id) },
                                onRatingChange = { rating -> 
                                    viewModel.updateOutfitRating(outfit, rating) 
                                },
                                onMarkAsWorn = { viewModel.markOutfitAsWorn(outfit) },
                                onDelete = { viewModel.deleteOutfit(outfit) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
        
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun OutfitCard(
    outfit: Outfit,
    onClick: () -> Unit,
    onRatingChange: (Float) -> Unit,
    onMarkAsWorn: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Outfit preview (first clothing item image or placeholder)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (outfit.clothingItems.isNotEmpty()) {
                    val firstItem = outfit.clothingItems.first()
                    val imageRequest = ImageRequest.Builder(context)
                        .data(File(firstItem.imagePath))
                        .crossfade(true)
                        .build()
                    
                    Image(
                        painter = rememberAsyncImagePainter(imageRequest),
                        contentDescription = outfit.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = outfit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("标记为已穿") },
                            onClick = {
                                onMarkAsWorn()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showDeleteConfirmation = true
                                showMenu = false
                            }
                        )
                    }
                }
                
                outfit.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "${outfit.clothingItems.size} 件衣服",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                ReadOnlyRatingBar(
                    rating = outfit.rating,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                outfit.lastWorn?.let { lastWorn ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(
                        text = "上次穿着: ${dateFormat.format(Date(lastWorn))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("删除穿搭") },
            text = { Text("确定要删除穿搭 \"${outfit.name}\" 吗？此操作不可撤销，但衣服项目将被保留。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyOutfitContent(
    onCreateOutfitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Style,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "还没有穿搭",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "创建第一个穿搭来搭配你的衣服",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onCreateOutfitClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建穿搭")
        }
    }
}

@Composable
private fun RatingFilterDialog(
    currentMinRating: Float?,
    currentMaxRating: Float?,
    onRatingFilterChange: (Float?, Float?) -> Unit,
    onDismiss: () -> Unit
) {
    var minRating by remember { mutableStateOf(currentMinRating ?: 1f) }
    var maxRating by remember { mutableStateOf(currentMaxRating ?: 5f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("评分筛选") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("选择评分范围:")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最低:")
                    RatingBar(
                        rating = minRating,
                        onRatingChange = { rating ->
                            minRating = rating
                            if (rating > maxRating) {
                                maxRating = rating
                            }
                        }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最高:")
                    RatingBar(
                        rating = maxRating,
                        onRatingChange = { rating ->
                            maxRating = rating
                            if (rating < minRating) {
                                minRating = rating
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRatingFilterChange(minRating, maxRating)
                }
            ) {
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}