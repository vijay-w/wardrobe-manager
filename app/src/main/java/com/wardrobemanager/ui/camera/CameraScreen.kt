package com.wardrobemanager.ui.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.wardrobemanager.ui.permissions.PermissionManager
import java.io.File

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onImageCaptured: (String) -> Unit = {},
    cameraManager: CameraManager = hiltViewModel<CameraScreenViewModel>().cameraManager,
    permissionManager: PermissionManager = hiltViewModel<CameraScreenViewModel>().permissionManager
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember { 
        mutableStateOf(permissionManager.hasCameraPermission()) 
    }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isCameraReady by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            errorMessage = "需要相机权限来拍照"
            showError = true
        }
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "需要相机权限",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "请授予相机权限以拍摄照片",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { 
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("授予权限")
                }
                TextButton(onClick = onNavigateBack) {
                    Text("取消")
                }
            }
        }
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    cameraManager.setupCamera(
                        lifecycleOwner = lifecycleOwner,
                        previewView = this,
                        onCameraReady = { isCameraReady = true },
                        onError = { exception ->
                            errorMessage = "相机初始化失败: ${exception.message}"
                            showError = true
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Camera controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
            
            // Capture button
            IconButton(
                onClick = {
                    if (isCameraReady) {
                        cameraManager.takePhoto(
                            onImageCaptured = { file ->
                                onImageCaptured(file.absolutePath)
                            },
                            onError = { exception ->
                                errorMessage = "拍照失败: ${exception.message}"
                                showError = true
                            }
                        )
                    }
                },
                enabled = isCameraReady,
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isCameraReady) Color.White else Color.Gray,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "拍照",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Placeholder for symmetry
            Spacer(modifier = Modifier.size(56.dp))
        }
        
        // Loading indicator
        if (!isCameraReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
    
    // Error dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("错误") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun CameraScreenViewModel(): CameraScreenViewModel = hiltViewModel()

class CameraScreenViewModel @javax.inject.Inject constructor(
    val cameraManager: CameraManager,
    val permissionManager: PermissionManager
) : androidx.lifecycle.ViewModel() {
    
    override fun onCleared() {
        super.onCleared()
        cameraManager.cleanup()
    }
}