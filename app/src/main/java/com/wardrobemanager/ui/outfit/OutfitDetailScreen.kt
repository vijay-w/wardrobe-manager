package com.wardrobemanager.ui.outfit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.ui.components.RatingBar
import com.wardrobemanager.ui.components.ReadOnlyRatingBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitDetailScreen(
    outfitId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OutfitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableClothingItems by viewModel.availableClothingItems.collectAsState()
    
    LaunchedEffect(outfitId) {
        viewModel.loadOutfit(outfitId)
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text(uiState.outfit?.name ?: "穿搭详情") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                if (!uiState.isEditing) {
                    IconButton(onClick = viewModel::startEditing) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑"
                        )
                    }
                    IconButton(onClick = viewModel::deleteOutfit) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除"
                        )
                    }
                } else {
                    TextButton(onClick = viewModel::saveOutfit) {
                        Text("保存")
                    }
                    TextButton(onClick = viewModel::cancelEditing) {
                        Text("取消")
                    }
                }
            }
        )
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.outfit != null -> {
                    OutfitDetailContent(
                        outfit = uiState.outfit!!,
                        isEditing = uiState.isEditing,
                        availableClothingItems = availableClothingItems,
                        onNameChange = viewModel::updateOutfitName,
                        onDescriptionChange = viewModel::updateOutfitDescription,
                        onRatingChange = viewModel::updateOutfitRating,
                        onAddClothingItem = viewModel::addClothingItemToOutfit,
                        onRemoveClothingItem = viewModel::removeClothingItemFromOutfit,
                        onMarkAsWorn = viewModel::markAsWorn
                    )
                }
                else -> {
                    Text(
                        text = "穿搭未找到",
                        modifier = Modifier.align(Alignment.Center)
                    )
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
private fun OutfitDetailContent(
    outfit: Outfit,
    isEditing: Boolean,
    availableClothingItems: List<ClothingItem>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRatingChange: (Float) -> Unit,
    onAddClothingItem: (ClothingItem) -> Unit,
    onRemoveClothingItem: (ClothingItem) -> Unit,
    onMarkAsWorn: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Basic Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name
                    if (isEditing) {
                        OutlinedTextField(
                            value = outfit.name,
                            onValueChange = onNameChange,
                            label = { Text("穿搭名称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = outfit.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Description
                    if (isEditing) {
                        OutlinedTextField(
                            value = outfit.description ?: "",
                            onValueChange = onDescriptionChange,
                            label = { Text("描述") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    } else {
                        outfit.description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "评分:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (isEditing) {
                            RatingBar(
                                rating = outfit.rating,
                                onRatingChange = onRatingChange
                            )
                        } else {
                            ReadOnlyRatingBar(rating = outfit.rating)
                        }
                    }
                    
                    // Creation and last worn dates
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = "创建时间: ${dateFormat.format(Date(outfit.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    outfit.lastWorn?.let { lastWorn ->
                        Text(
                            text = "上次穿着: ${dateFormat.format(Date(lastWorn))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Mark as worn button
                    if (!isEditing) {
                        Button(
                            onClick = onMarkAsWorn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("标记为已穿")
                        }
                    }
                }
            }
        }
        
        item {
            // Clothing Items Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "包含的衣服 (${outfit.clothingItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (outfit.clothingItems.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(outfit.clothingItems) { clothingItem ->
                                ClothingItemCard(
                                    clothingItem = clothingItem,
                                    isEditing = isEditing,
                                    onRemove = { onRemoveClothingItem(clothingItem) }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "此穿搭还没有添加衣服",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Available clothing items for editing
        if (isEditing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "可添加的衣服",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        val availableItems = availableClothingItems.filter { available ->
                            outfit.clothingItems.none { it.id == available.id }
                        }
                        
                        if (availableItems.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableItems) { clothingItem ->
                                    ClothingItemCard(
                                        clothingItem = clothingItem,
                                        isEditing = false,
                                        onAdd = { onAddClothingItem(clothingItem) }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "没有可添加的衣服",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClothingItemCard(
    clothingItem: ClothingItem,
    isEditing: Boolean,
    onRemove: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                val imageRequest = ImageRequest.Builder(context)
                    .data(File(clothingItem.imagePath))
                    .crossfade(true)
                    .build()
                
                Image(
                    painter = rememberAsyncImagePainter(imageRequest),
                    contentDescription = clothingItem.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Remove button for editing mode
                if (isEditing && onRemove != null) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = "移除",
                            tint = Color.Red
                        )
                    }
                }
                
                // Add button for available items
                if (onAdd != null) {
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "添加",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Text(
                text = clothingItem.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            ReadOnlyRatingBar(
                rating = clothingItem.rating,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}