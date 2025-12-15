package com.wardrobemanager.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.coroutines.launch
import com.wardrobemanager.R
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.ui.components.RatingBar
import com.wardrobemanager.ui.components.ThumbnailImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    onNavigateToAddClothing: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onClothingItemClick: (ClothingItem) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WardrobeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Error handling
    com.wardrobemanager.ui.error.ErrorSnackbar(
        errorInfo = errorState,
        snackbarHostState = snackbarHostState,
        onDismiss = { viewModel.clearError() },
        onRetry = { viewModel.retryLastOperation() },
        onAction = { errorInfo ->
            // Handle specific error actions if needed
        },
        scope = scope
    )
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text(stringResource(R.string.nav_wardrobe)) },
            actions = {
                IconButton(onClick = onNavigateToAddClothing) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加衣服"
                    )
                }
                IconButton(onClick = { viewModel.toggleFilterExpansion() }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filter)
                    )
                }
            }
        )
        
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Filter Section
        if (uiState.isFilterExpanded) {
            FilterSection(
                selectedCategory = uiState.selectedCategory,
                minRating = uiState.minRating,
                maxRating = uiState.maxRating,
                minPrice = uiState.minPrice,
                maxPrice = uiState.maxPrice,
                sortByRating = uiState.sortByRating,
                onCategoryChange = viewModel::updateCategory,
                onRatingFilterChange = viewModel::updateRatingFilter,
                onPriceFilterChange = viewModel::updatePriceFilter,
                onSortToggle = viewModel::toggleSortByRating,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.clothingItems.isEmpty() -> {
                    EmptyWardrobeContent(
                        onAddClothingClick = onNavigateToAddClothing,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    ClothingGrid(
                        clothingItems = uiState.clothingItems,
                        onClothingItemClick = onClothingItemClick,
                        onRatingChange = viewModel::updateClothingRating,
                        onMarkAsWorn = viewModel::markAsWorn,
                        onDeleteItem = viewModel::deleteClothingItem
                    )
                }
            }
        }
        
        // Snackbar Host for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除搜索"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun FilterSection(
    selectedCategory: ClothingCategory?,
    minRating: Float?,
    maxRating: Float?,
    minPrice: Double?,
    maxPrice: Double?,
    sortByRating: Boolean,
    onCategoryChange: (ClothingCategory?) -> Unit,
    onRatingFilterChange: (Float?, Float?) -> Unit,
    onPriceFilterChange: (Double?, Double?) -> Unit,
    onSortToggle: () -> Unit,
    onClearFilters: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "筛选选项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearFilters) {
                    Text("清除筛选")
                }
            }
            
            // Category Filter
            Text("分类", style = MaterialTheme.typography.labelMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategoryChange(null) },
                        label = { Text("全部") }
                    )
                }
                items(ClothingCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category.displayName) }
                    )
                }
            }
            
            // Sort Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("按评分排序", style = MaterialTheme.typography.labelMedium)
                Switch(
                    checked = sortByRating,
                    onCheckedChange = { onSortToggle() }
                )
            }
        }
    }
}

@Composable
private fun ClothingGrid(
    clothingItems: List<ClothingItem>,
    onClothingItemClick: (ClothingItem) -> Unit,
    onRatingChange: (ClothingItem, Float) -> Unit,
    onMarkAsWorn: (ClothingItem) -> Unit,
    onDeleteItem: (ClothingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(clothingItems) { clothingItem ->
            ClothingItemCard(
                clothingItem = clothingItem,
                onClick = { onClothingItemClick(clothingItem) },
                onRatingChange = { rating -> onRatingChange(clothingItem, rating) },
                onMarkAsWorn = { onMarkAsWorn(clothingItem) },
                onDelete = { onDeleteItem(clothingItem) }
            )
        }
    }
}

@Composable
private fun ClothingItemCard(
    clothingItem: ClothingItem,
    onClick: () -> Unit,
    onRatingChange: (Float) -> Unit,
    onMarkAsWorn: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                ThumbnailImage(
                    imagePath = clothingItem.imagePath,
                    contentDescription = clothingItem.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                
                // Menu button
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多选项",
                        tint = Color.White
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
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = clothingItem.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = clothingItem.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                RatingBar(
                    rating = clothingItem.rating,
                    onRatingChange = onRatingChange,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                clothingItem.price?.let { price ->
                    Text(
                        text = "¥${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWardrobeContent(
    onAddClothingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Checkroom,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "衣橱是空的",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "添加第一件衣服开始管理你的穿搭",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(onClick = onAddClothingClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加衣服")
        }
    }
}