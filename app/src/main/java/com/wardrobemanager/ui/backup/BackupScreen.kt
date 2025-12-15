package com.wardrobemanager.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.backup.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // File picker for restore
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.restoreFromUri(context, it) }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadBackupFiles()
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("备份与恢复") }
        )
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Section
            item {
                BackupSection(
                    onCreateBackup = viewModel::createBackup,
                    isCreatingBackup = uiState.isCreatingBackup,
                    backupProgress = uiState.backupProgress
                )
            }
            
            // Restore Section
            item {
                RestoreSection(
                    onRestoreFromFile = { restoreFileLauncher.launch("application/zip") },
                    isRestoring = uiState.isRestoring,
                    restoreProgress = uiState.restoreProgress
                )
            }
            
            // Backup Files List
            item {
                Text(
                    text = "本地备份文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (uiState.backupFiles.isEmpty()) {
                item {
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
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "暂无备份文件",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.backupFiles) { backupInfo ->
                    BackupFileCard(
                        backupInfo = backupInfo,
                        onRestore = { viewModel.restoreFromFile(backupInfo.file) },
                        onDelete = { viewModel.deleteBackupFile(backupInfo.file) },
                        onShare = { viewModel.shareBackupFile(context, backupInfo.file) }
                    )
                }
            }
        }
    }
    
    // Error/Success Messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
        }
        
        AlertDialog(
            onDismissRequest = viewModel::clearMessage,
            title = { 
                Text(if (message.startsWith("成功") || message.startsWith("完成")) "成功" else "提示") 
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMessage) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun BackupSection(
    onCreateBackup: () -> Unit,
    isCreatingBackup: Boolean,
    backupProgress: BackupProgress?,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "创建备份",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "备份您的所有衣服、穿搭和图片数据到本地文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isCreatingBackup && backupProgress != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = backupProgress.percentage / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${backupProgress.percentage}% - ${backupProgress.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = onCreateBackup,
                enabled = !isCreatingBackup,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreatingBackup) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCreatingBackup) "创建中..." else "创建备份")
            }
        }
    }
}

@Composable
private fun RestoreSection(
    onRestoreFromFile: () -> Unit,
    isRestoring: Boolean,
    restoreProgress: RestoreProgress?,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "恢复数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "从备份文件恢复您的衣橱数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isRestoring && restoreProgress != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = restoreProgress.percentage / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${restoreProgress.percentage}% - ${restoreProgress.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            OutlinedButton(
                onClick = onRestoreFromFile,
                enabled = !isRestoring,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRestoring) "恢复中..." else "选择备份文件")
            }
        }
    }
}

@Composable
private fun BackupFileCard(
    backupInfo: BackupFileInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(backupInfo.timestamp)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = formatFileSize(backupInfo.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${backupInfo.clothingItemsCount} 件衣服",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${backupInfo.outfitsCount} 个穿搭",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("恢复")
                }
                
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("分享")
                }
                
                OutlinedButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除备份") },
            text = { Text("确定要删除这个备份文件吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    
    return when {
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}

// ViewModel for BackupScreen
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun loadBackupFiles() {
        viewModelScope.launch {
            try {
                val backupFiles = backupManager.getBackupFiles()
                val backupInfos = backupFiles.mapNotNull { file ->
                    backupManager.getBackupFileInfo(file)
                }
                _uiState.value = _uiState.value.copy(backupFiles = backupInfos)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "加载备份文件失败: ${e.message}"
                )
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBackup = true)
            
            val result = backupManager.createBackup { progress ->
                _uiState.value = _uiState.value.copy(backupProgress = progress)
            }
            
            when (result) {
                is BackupResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingBackup = false,
                        backupProgress = null,
                        message = "备份创建成功"
                    )
                    loadBackupFiles() // Refresh the list
                }
                is BackupResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingBackup = false,
                        backupProgress = null,
                        message = result.message
                    )
                }
            }
        }
    }

    fun restoreFromFile(backupFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true)
            
            val result = backupManager.restoreBackup(backupFile) { progress ->
                _uiState.value = _uiState.value.copy(restoreProgress = progress)
            }
            
            when (result) {
                is RestoreResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        restoreProgress = null,
                        message = "恢复完成：${result.clothingItemsCount} 件衣服，${result.outfitsCount} 个穿搭"
                    )
                }
                is RestoreResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        restoreProgress = null,
                        message = result.message
                    )
                }
            }
        }
    }

    fun restoreFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                // Copy URI content to temporary file
                val tempFile = File(context.cacheDir, "temp_backup.zip")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                restoreFromFile(tempFile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "读取备份文件失败: ${e.message}"
                )
            }
        }
    }

    fun deleteBackupFile(backupFile: File) {
        viewModelScope.launch {
            if (backupManager.deleteBackupFile(backupFile)) {
                _uiState.value = _uiState.value.copy(
                    message = "备份文件已删除"
                )
                loadBackupFiles() // Refresh the list
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "删除备份文件失败"
                )
            }
        }
    }

    fun shareBackupFile(context: android.content.Context, backupFile: File) {
        try {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "application/zip"
                putExtra(android.content.Intent.EXTRA_STREAM, 
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        backupFile
                    )
                )
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "分享备份文件"))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                message = "分享失败: ${e.message}"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class BackupUiState(
    val isCreatingBackup: Boolean = false,
    val isRestoring: Boolean = false,
    val backupProgress: BackupProgress? = null,
    val restoreProgress: RestoreProgress? = null,
    val backupFiles: List<BackupFileInfo> = emptyList(),
    val message: String? = null
)