package com.wardrobemanager.ui.outfit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wardrobemanager.R
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOutfitScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateOutfitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAvailableClothingItems()
    }
    
    // Handle successful creation
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.error == null && 
            uiState.name.isEmpty() && uiState.selectedClothingItems.isEmpty()) {
            // Form was reset, meaning creation was successful
            onNavigateBack()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("创建穿搭") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = viewModel::saveOutfit,
                    enabled = !uiState.isLoading && uiState.name.isNotBlank() && 
                             uiState.selectedClothingItems.isNotEmpty()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Outfit Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("穿搭名称") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("描述（可选）") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Selected Items Preview
            if (uiState.selectedClothingItems.isNotEmpty()) {
                Text(
                    text = "已选择的衣服 (${uiState.selectedClothingItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.selectedClothingItems) { clothingItem ->
                        SelectedClothingItemCard(
                            clothingItem = clothingItem,
                            onRemove = { viewModel.removeClothingItem(clothingItem) }
                        )
                    }
                }
            }
            
            // Available Items
            Text(
                text = "选择衣服",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (uiState.availableClothingItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "没有可用的衣服",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "请先添加一些衣服到衣橱",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(400.dp) // Fixed height for scrollable grid
                ) {
                    items(uiState.availableClothingItems) { clothingItem ->
                        val isSelected = uiState.selectedClothingItems.contains(clothingItem)
                        ClothingItemSelectionCard(
                            clothingItem = clothingItem,
                            isSelected = isSelected,
                            onToggleSelection = {
                                if (isSelected) {
                                    viewModel.removeClothingItem(clothingItem)
                                } else {
                                    viewModel.addClothingItem(clothingItem)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
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
private fun SelectedClothingItemCard(
    clothingItem: ClothingItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.width(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(File(clothingItem.imagePath))
                        .crossfade(true)
                        .build()
                    
                    Image(
                        painter = rememberAsyncImagePainter(imageRequest),
                        contentDescription = clothingItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Text(
                    text = clothingItem.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp),
                    maxLines = 1
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ClothingItemSelectionCard(
    clothingItem: ClothingItem,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onToggleSelection() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(File(clothingItem.imagePath))
                        .crossfade(true)
                        .build()
                    
                    Image(
                        painter = rememberAsyncImagePainter(imageRequest),
                        contentDescription = clothingItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Text(
                    text = clothingItem.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp),
                    maxLines = 1
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(2.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

// ViewModel for CreateOutfitScreen
@HiltViewModel
class CreateOutfitViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothingRepository: ClothingRepository
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(CreateOutfitUiState())
    val uiState: StateFlow<CreateOutfitUiState> = _uiState.asStateFlow()

    fun loadAvailableClothingItems() {
        viewModelScope.launch {
            clothingRepository.getAllClothing().collect { items ->
                _uiState.value = _uiState.value.copy(availableClothingItems = items)
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "名称不能为空" else null
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun addClothingItem(clothingItem: ClothingItem) {
        val currentItems = _uiState.value.selectedClothingItems
        if (!currentItems.contains(clothingItem)) {
            _uiState.value = _uiState.value.copy(
                selectedClothingItems = currentItems + clothingItem
            )
        }
    }

    fun removeClothingItem(clothingItem: ClothingItem) {
        val currentItems = _uiState.value.selectedClothingItems
        _uiState.value = _uiState.value.copy(
            selectedClothingItems = currentItems.filter { it.id != clothingItem.id }
        )
    }

    fun saveOutfit() {
        val currentState = _uiState.value
        
        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(nameError = "名称不能为空")
            return
        }
        
        if (currentState.selectedClothingItems.isEmpty()) {
            _uiState.value = currentState.copy(error = "请至少选择一件衣服")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            try {
                val outfit = Outfit(
                    name = currentState.name.trim(),
                    description = if (currentState.description.isBlank()) null else currentState.description.trim(),
                    clothingItems = currentState.selectedClothingItems
                )
                
                outfitRepository.insertOutfit(outfit)
                
                // Reset form after successful save
                _uiState.value = CreateOutfitUiState(
                    availableClothingItems = currentState.availableClothingItems
                )
                
            } catch (exception: Exception) {
                _uiState.value = currentState.copy(
                    error = "保存失败: ${exception.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}