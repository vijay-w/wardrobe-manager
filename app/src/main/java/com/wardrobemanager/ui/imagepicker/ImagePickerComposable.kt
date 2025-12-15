package com.wardrobemanager.ui.imagepicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.canhub.cropper.CropImageContract
import com.wardrobemanager.R
import com.wardrobemanager.ui.components.OptimizedImage
import java.io.File

@Composable
fun ImagePicker(
    selectedImageUri: Uri? = null,
    selectedImagePath: String = "",
    onImageSelected: (Uri) -> Unit,
    onCameraRequested: () -> Unit,
    modifier: Modifier = Modifier,
    imagePickerManager: ImagePickerManager = hiltViewModel<ImagePickerViewModel>().imagePickerManager
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            // Launch crop activity
            cropLauncher.launch(
                imagePickerManager.createCropImageContractOptions(selectedUri)
            )
        }
    }
    
    // Crop launcher
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                onImageSelected(croppedUri)
            }
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image display area
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showImageSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null || selectedImagePath.isNotEmpty()) {
                val imagePath = selectedImagePath.ifEmpty { 
                    selectedImageUri?.path ?: ""
                }
                
                OptimizedImage(
                    imagePath = imagePath,
                    contentDescription = "选中的图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    shape = RoundedCornerShape(12.dp),
                    thumbnailSize = 300
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加图片",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击添加图片",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCameraRequested,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.take_photo))
            }
            
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        imagePickerManager.createPickVisualMediaRequest()
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.choose_from_gallery))
            }
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text(stringResource(R.string.image_source)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            onCameraRequested()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.take_photo))
                    }
                    
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                imagePickerManager.createPickVisualMediaRequest()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.choose_from_gallery))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ImagePickerViewModel(): ImagePickerViewModel = hiltViewModel()

class ImagePickerViewModel @javax.inject.Inject constructor(
    val imagePickerManager: ImagePickerManager
) : androidx.lifecycle.ViewModel()