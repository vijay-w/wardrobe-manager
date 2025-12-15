package com.wardrobemanager.ui.error

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(
    private val context: Context
) {
    
    fun handleError(
        error: Throwable,
        userMessage: String? = null,
        showToast: Boolean = false,
        context: String? = null
    ): ErrorInfo {
        val errorInfo = when (error) {
            // Network-related errors
            is UnknownHostException -> ErrorInfo(
                type = ErrorType.NETWORK_ERROR,
                userMessage = userMessage ?: "无法连接到网络",
                technicalMessage = error.message ?: "Unknown host",
                isRecoverable = true,
                suggestedAction = "请检查网络连接并重试",
                actionType = ErrorActionType.RETRY,
                context = context
            )
            is ConnectException -> ErrorInfo(
                type = ErrorType.NETWORK_ERROR,
                userMessage = userMessage ?: "网络连接超时",
                technicalMessage = error.message ?: "Connection failed",
                isRecoverable = true,
                suggestedAction = "请检查网络连接或稍后重试",
                actionType = ErrorActionType.RETRY,
                context = context
            )
            is SocketTimeoutException -> ErrorInfo(
                type = ErrorType.NETWORK_ERROR,
                userMessage = userMessage ?: "网络请求超时",
                technicalMessage = error.message ?: "Socket timeout",
                isRecoverable = true,
                suggestedAction = "网络较慢，请稍后重试",
                actionType = ErrorActionType.RETRY,
                context = context
            )
            
            // Storage-related errors
            is java.io.IOException -> {
                when {
                    error.message?.contains("ENOSPC") == true -> ErrorInfo(
                        type = ErrorType.STORAGE_ERROR,
                        userMessage = userMessage ?: "存储空间不足",
                        technicalMessage = error.message ?: "No space left on device",
                        isRecoverable = true,
                        suggestedAction = "请清理存储空间后重试",
                        actionType = ErrorActionType.OPEN_SETTINGS,
                        context = context
                    )
                    error.message?.contains("Permission denied") == true -> ErrorInfo(
                        type = ErrorType.PERMISSION_ERROR,
                        userMessage = userMessage ?: "存储权限被拒绝",
                        technicalMessage = error.message ?: "Storage permission denied",
                        isRecoverable = true,
                        suggestedAction = "请授予存储权限",
                        actionType = ErrorActionType.OPEN_SETTINGS,
                        context = context
                    )
                    else -> ErrorInfo(
                        type = ErrorType.IO_ERROR,
                        userMessage = userMessage ?: "文件操作失败",
                        technicalMessage = error.message ?: "Unknown IO error",
                        isRecoverable = true,
                        suggestedAction = "请检查存储空间并重试",
                        actionType = ErrorActionType.RETRY,
                        context = context
                    )
                }
            }
            is FileNotFoundException -> ErrorInfo(
                type = ErrorType.IO_ERROR,
                userMessage = userMessage ?: "文件未找到",
                technicalMessage = error.message ?: "File not found",
                isRecoverable = true,
                suggestedAction = "请重新选择文件",
                actionType = ErrorActionType.RETRY,
                context = context
            )
            
            // Database-related errors
            is android.database.sqlite.SQLiteException -> {
                when {
                    error.message?.contains("corrupt") == true -> ErrorInfo(
                        type = ErrorType.DATABASE_ERROR,
                        userMessage = userMessage ?: "数据库已损坏",
                        technicalMessage = error.message ?: "Database corruption",
                        isRecoverable = true,
                        suggestedAction = "建议从备份恢复数据",
                        actionType = ErrorActionType.RESTORE_BACKUP,
                        context = context
                    )
                    error.message?.contains("disk I/O error") == true -> ErrorInfo(
                        type = ErrorType.DATABASE_ERROR,
                        userMessage = userMessage ?: "数据库读写错误",
                        technicalMessage = error.message ?: "Database I/O error",
                        isRecoverable = true,
                        suggestedAction = "请检查存储空间并重试",
                        actionType = ErrorActionType.RETRY,
                        context = context
                    )
                    else -> ErrorInfo(
                        type = ErrorType.DATABASE_ERROR,
                        userMessage = userMessage ?: "数据库操作失败",
                        technicalMessage = error.message ?: "Database error",
                        isRecoverable = true,
                        suggestedAction = "请重试操作",
                        actionType = ErrorActionType.RETRY,
                        context = context
                    )
                }
            }
            
            // Permission-related errors
            is SecurityException -> ErrorInfo(
                type = ErrorType.PERMISSION_ERROR,
                userMessage = userMessage ?: "权限不足",
                technicalMessage = error.message ?: "Permission denied",
                isRecoverable = true,
                suggestedAction = "请授予必要的权限",
                actionType = ErrorActionType.OPEN_SETTINGS,
                context = context
            )
            
            // Validation errors
            is IllegalArgumentException -> ErrorInfo(
                type = ErrorType.VALIDATION_ERROR,
                userMessage = userMessage ?: "输入数据无效",
                technicalMessage = error.message ?: "Invalid argument",
                isRecoverable = true,
                suggestedAction = "请检查输入数据格式",
                actionType = ErrorActionType.NONE,
                context = context
            )
            
            // Memory errors
            is OutOfMemoryError -> ErrorInfo(
                type = ErrorType.MEMORY_ERROR,
                userMessage = userMessage ?: "内存不足",
                technicalMessage = error.message ?: "Out of memory",
                isRecoverable = false,
                suggestedAction = "请关闭其他应用或重启设备",
                actionType = ErrorActionType.NONE,
                context = context
            )
            
            // Backup/Restore errors
            is kotlinx.serialization.SerializationException -> ErrorInfo(
                type = ErrorType.BACKUP_ERROR,
                userMessage = userMessage ?: "数据格式错误",
                technicalMessage = error.message ?: "Serialization error",
                isRecoverable = true,
                suggestedAction = "备份文件可能已损坏，请选择其他备份文件",
                actionType = ErrorActionType.RETRY,
                context = context
            )
            
            // Image processing errors
            is java.lang.RuntimeException -> {
                when {
                    error.message?.contains("Unable to decode") == true -> ErrorInfo(
                        type = ErrorType.IMAGE_ERROR,
                        userMessage = userMessage ?: "图片格式不支持",
                        technicalMessage = error.message ?: "Image decode error",
                        isRecoverable = true,
                        suggestedAction = "请选择JPEG或PNG格式的图片",
                        actionType = ErrorActionType.RETRY,
                        context = context
                    )
                    else -> ErrorInfo(
                        type = ErrorType.UNKNOWN_ERROR,
                        userMessage = userMessage ?: "发生未知错误",
                        technicalMessage = error.message ?: "Unknown runtime error",
                        isRecoverable = false,
                        suggestedAction = "请重试或联系支持",
                        actionType = ErrorActionType.RETRY,
                        context = context
                    )
                }
            }
            
            else -> ErrorInfo(
                type = ErrorType.UNKNOWN_ERROR,
                userMessage = userMessage ?: "发生未知错误",
                technicalMessage = error.message ?: "Unknown error",
                isRecoverable = false,
                suggestedAction = "请重试或联系支持",
                actionType = ErrorActionType.RETRY,
                context = context
            )
        }
        
        if (showToast) {
            Toast.makeText(context, errorInfo.userMessage, Toast.LENGTH_LONG).show()
        }
        
        // Log error for debugging
        logError(errorInfo, error)
        
        return errorInfo
    }
    
    private fun logError(errorInfo: ErrorInfo, throwable: Throwable) {
        // In a real app, you would use a proper logging framework like Timber
        val contextInfo = errorInfo.context?.let { " [Context: $it]" } ?: ""
        println("ERROR [${errorInfo.type}]$contextInfo: ${errorInfo.userMessage}")
        println("Technical: ${errorInfo.technicalMessage}")
        println("Suggested Action: ${errorInfo.suggestedAction}")
        println("Action Type: ${errorInfo.actionType}")
        println("Timestamp: ${errorInfo.timestamp}")
        throwable.printStackTrace()
    }
    
    fun createSafeOperation<T>(
        operation: suspend () -> T,
        onError: (ErrorInfo) -> Unit,
        onSuccess: (T) -> Unit = {},
        context: String? = null
    ): suspend () -> Unit = {
        try {
            val result = operation()
            onSuccess(result)
        } catch (e: Exception) {
            val errorInfo = handleError(e, context = context)
            onError(errorInfo)
        }
    }
    
    fun createSafeOperationWithRetry<T>(
        operation: suspend () -> T,
        onError: (ErrorInfo) -> Unit,
        onSuccess: (T) -> Unit = {},
        maxRetries: Int = 3,
        context: String? = null
    ): suspend () -> Unit = {
        var attempts = 0
        var lastError: ErrorInfo? = null
        
        while (attempts < maxRetries) {
            try {
                val result = operation()
                onSuccess(result)
                return@suspend
            } catch (e: Exception) {
                attempts++
                lastError = handleError(e, context = context)
                
                if (!shouldRetryOperation(lastError) || attempts >= maxRetries) {
                    onError(lastError)
                    return@suspend
                }
                
                // Wait before retry (exponential backoff)
                kotlinx.coroutines.delay(1000L * attempts)
            }
        }
        
        lastError?.let { onError(it) }
    }
    
    fun isDataCorruption(error: Throwable): Boolean {
        return error is android.database.sqlite.SQLiteException ||
               error is kotlinx.serialization.SerializationException ||
               (error is java.io.IOException && error.message?.contains("corrupt") == true)
    }
    
    fun shouldRetryOperation(errorInfo: ErrorInfo): Boolean {
        return errorInfo.isRecoverable && 
               errorInfo.type in listOf(
                   ErrorType.NETWORK_ERROR,
                   ErrorType.IO_ERROR,
                   ErrorType.DATABASE_ERROR
               )
    }
    
    fun executeErrorAction(errorInfo: ErrorInfo) {
        when (errorInfo.actionType) {
            ErrorActionType.OPEN_SETTINGS -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            ErrorActionType.OPEN_STORAGE_SETTINGS -> {
                val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            ErrorActionType.RESTORE_BACKUP -> {
                // This would typically trigger a backup restore flow
                // Implementation depends on your backup system
            }
            ErrorActionType.RETRY, ErrorActionType.NONE -> {
                // No automatic action needed
            }
        }
    }
    
    fun getNetworkErrorMessage(error: Throwable): String {
        return when (error) {
            is UnknownHostException -> "无法连接到服务器，请检查网络连接"
            is ConnectException -> "连接超时，请检查网络或稍后重试"
            is SocketTimeoutException -> "网络请求超时，请稍后重试"
            else -> "网络错误，请检查连接"
        }
    }
    
    fun getStorageErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("ENOSPC") == true -> "存储空间不足，请清理后重试"
            error.message?.contains("Permission denied") == true -> "存储权限被拒绝，请在设置中授权"
            error is FileNotFoundException -> "文件未找到，请重新选择"
            else -> "存储操作失败，请重试"
        }
    }
}

data class ErrorInfo(
    val type: ErrorType,
    val userMessage: String,
    val technicalMessage: String,
    val isRecoverable: Boolean,
    val suggestedAction: String,
    val actionType: ErrorActionType = ErrorActionType.NONE,
    val context: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ErrorType {
    IO_ERROR,
    NETWORK_ERROR,
    PERMISSION_ERROR,
    VALIDATION_ERROR,
    MEMORY_ERROR,
    DATABASE_ERROR,
    BACKUP_ERROR,
    STORAGE_ERROR,
    IMAGE_ERROR,
    UNKNOWN_ERROR
}

enum class ErrorActionType {
    NONE,
    RETRY,
    OPEN_SETTINGS,
    OPEN_STORAGE_SETTINGS,
    RESTORE_BACKUP
}

@Composable
fun ErrorSnackbar(
    errorInfo: ErrorInfo?,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onAction: ((ErrorInfo) -> Unit)? = null,
    scope: CoroutineScope
) {
    LaunchedEffect(errorInfo) {
        errorInfo?.let { error ->
            scope.launch {
                val actionLabel = when (error.actionType) {
                    ErrorActionType.RETRY -> "重试"
                    ErrorActionType.OPEN_SETTINGS -> "设置"
                    ErrorActionType.OPEN_STORAGE_SETTINGS -> "存储设置"
                    ErrorActionType.RESTORE_BACKUP -> "恢复备份"
                    ErrorActionType.NONE -> if (error.isRecoverable) "确定" else null
                }
                
                val result = snackbarHostState.showSnackbar(
                    message = error.userMessage,
                    actionLabel = actionLabel,
                    withDismissAction = true,
                    duration = if (error.isRecoverable) 
                        androidx.compose.material3.SnackbarDuration.Long 
                    else 
                        androidx.compose.material3.SnackbarDuration.Short
                )
                
                when (result) {
                    androidx.compose.material3.SnackbarResult.ActionPerformed -> {
                        when (error.actionType) {
                            ErrorActionType.RETRY -> onRetry?.invoke()
                            else -> onAction?.invoke(error)
                        }
                    }
                    androidx.compose.material3.SnackbarResult.Dismissed -> {
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(
    errorInfo: ErrorInfo?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onAction: ((ErrorInfo) -> Unit)? = null
) {
    errorInfo?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                androidx.compose.material3.Text(
                    text = when (error.type) {
                        ErrorType.NETWORK_ERROR -> "网络错误"
                        ErrorType.STORAGE_ERROR -> "存储错误"
                        ErrorType.DATABASE_ERROR -> "数据库错误"
                        ErrorType.PERMISSION_ERROR -> "权限错误"
                        ErrorType.BACKUP_ERROR -> "备份错误"
                        ErrorType.IMAGE_ERROR -> "图片错误"
                        ErrorType.MEMORY_ERROR -> "内存错误"
                        ErrorType.VALIDATION_ERROR -> "输入错误"
                        else -> "系统错误"
                    }
                )
            },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(
                        text = error.userMessage,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    
                    if (error.suggestedAction.isNotBlank()) {
                        androidx.compose.foundation.layout.Spacer(
                            modifier = androidx.compose.ui.Modifier.height(8.dp)
                        )
                        androidx.compose.material3.Text(
                            text = "建议: ${error.suggestedAction}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                when (error.actionType) {
                    ErrorActionType.RETRY -> {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onRetry?.invoke()
                                onDismiss()
                            }
                        ) {
                            androidx.compose.material3.Text("重试")
                        }
                    }
                    ErrorActionType.OPEN_SETTINGS -> {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAction?.invoke(error)
                                onDismiss()
                            }
                        ) {
                            androidx.compose.material3.Text("打开设置")
                        }
                    }
                    ErrorActionType.OPEN_STORAGE_SETTINGS -> {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAction?.invoke(error)
                                onDismiss()
                            }
                        ) {
                            androidx.compose.material3.Text("存储设置")
                        }
                    }
                    ErrorActionType.RESTORE_BACKUP -> {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAction?.invoke(error)
                                onDismiss()
                            }
                        ) {
                            androidx.compose.material3.Text("恢复备份")
                        }
                    }
                    ErrorActionType.NONE -> {
                        androidx.compose.material3.TextButton(
                            onClick = onDismiss
                        ) {
                            androidx.compose.material3.Text("确定")
                        }
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) {
                    androidx.compose.material3.Text("取消")
                }
            }
        )
    }
}

/**
 * Global error state manager for the application
 */
class GlobalErrorState {
    private val _errorInfo = MutableStateFlow<ErrorInfo?>(null)
    val errorInfo: StateFlow<ErrorInfo?> = _errorInfo.asStateFlow()
    
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    
    fun showError(errorInfo: ErrorInfo, useDialog: Boolean = false) {
        _errorInfo.value = errorInfo
        _showDialog.value = useDialog
    }
    
    fun clearError() {
        _errorInfo.value = null
        _showDialog.value = false
    }
}