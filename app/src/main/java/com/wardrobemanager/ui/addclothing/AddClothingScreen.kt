package com.wardrobemanager.ui.addclothing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wardrobemanager.R
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.ui.camera.CameraScreen
import com.wardrobemanager.ui.components.RatingBar
import com.wardrobemanager.ui.imagepicker.ImagePicker
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    savedStateHandle: androidx.navigation.SavedStateHandle? = null,
    modifier: Modifier = Modifier,
    viewModel: AddClothingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }
    
    // Handle captured image from camera
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<String>(com.wardrobemanager.ui.navigation.NavigationKeys.CAPTURED_IMAGE_PATH)?.let { imagePath ->
            viewModel.handleCameraImage(java.io.File(imagePath))
            savedStateHandle.remove<String>(com.wardrobemanager.ui.navigation.NavigationKeys.CAPTURED_IMAGE_PATH)
        }
    }
    
    // Handle successful save
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.error == null && 
            uiState.name.isEmpty() && uiState.imagePath.isEmpty()) {
            // Form was reset, meaning save was successful
            onNavigateBack()
        }
    }
    
    if (showCamera) {
        CameraScreen(
            onImageCaptured = { imageFile ->
                viewModel.handleCameraImage(imageFile)
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = { Text(stringResource(R.string.add_clothing)) },
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
                        onClick = { viewModel.onEvent(AddClothingEvent.SaveClothing) },
                        enabled = !uiState.isLoading
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
                // Image Picker
                ImagePicker(
                    selectedImageUri = uiState.imageUri,
                    selectedImagePath = uiState.imagePath,
                    onImageSelected = { uri ->
                        viewModel.onEvent(AddClothingEvent.ImageSelected(uri))
                    },
                    onCameraRequested = onNavigateToCamera
                )
                
                if (uiState.isImageProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("处理图片中...")
                    }
                }
                
                // Name Field
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(AddClothingEvent.NameChanged(it)) },
                    label = { Text(stringResource(R.string.clothing_name)) },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Selection
                CategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = { viewModel.onEvent(AddClothingEvent.CategoryChanged(it)) }
                )
                
                // Rating
                Column {
                    Text(
                        text = stringResource(R.string.clothing_rating),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingBar(
                        rating = uiState.rating,
                        onRatingChange = { viewModel.onEvent(AddClothingEvent.RatingChanged(it)) },
                        starSize = 24.dp
                    )
                }
                
                // Price Field
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { viewModel.onEvent(AddClothingEvent.PriceChanged(it)) },
                    label = { Text(stringResource(R.string.clothing_price)) },
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Purchase Link Field
                OutlinedTextField(
                    value = uiState.purchaseLink,
                    onValueChange = { viewModel.onEvent(AddClothingEvent.PurchaseLinkChanged(it)) },
                    label = { Text(stringResource(R.string.purchase_link)) },
                    isError = uiState.linkError != null,
                    supportingText = uiState.linkError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Notes Field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(AddClothingEvent.NotesChanged(it)) },
                    label = { Text(stringResource(R.string.notes)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error message
        }
        
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddClothingEvent.ClearError) },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AddClothingEvent.ClearError) }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: ClothingCategory,
    onCategorySelected: (ClothingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.clothing_category),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClothingCategory.values().chunked(2).forEach { categoryRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryRow.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of categories
                    if (categoryRow.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}