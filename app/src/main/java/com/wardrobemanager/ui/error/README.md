# Global Error Handling System

This document describes the comprehensive global error handling system implemented for the Wardrobe Manager application.

## Overview

The global error handling system provides:
- Unified error categorization and handling
- User-friendly error messages in Chinese
- Automatic retry mechanisms
- Context-aware error reporting
- Consistent error UI components
- Integration with ViewModels and Repositories

## Components

### 1. ErrorHandler
The main error handling service that categorizes and processes exceptions.

**Features:**
- Categorizes errors by type (Network, Storage, Database, etc.)
- Provides user-friendly messages
- Suggests appropriate actions
- Supports retry mechanisms
- Logs errors for debugging

**Usage:**
```kotlin
@Inject
lateinit var errorHandler: ErrorHandler

val errorInfo = errorHandler.handleError(
    exception,
    userMessage = "自定义用户消息",
    context = "操作上下文"
)
```

### 2. RepositoryErrorHandler
Extension for repositories to handle errors consistently.

**Usage:**
```kotlin
@Inject
lateinit var errorHandler: RepositoryErrorHandler

// Database operations
suspend fun saveData(data: Data) {
    errorHandler.withDatabaseErrorHandling("saveData") {
        dao.insert(data)
    }
}

// File operations
suspend fun saveFile(file: File) {
    errorHandler.withFileErrorHandling("saveFile") {
        fileSystem.save(file)
    }
}

// Network operations
suspend fun fetchData() {
    errorHandler.withNetworkErrorHandling("fetchData") {
        api.getData()
    }
}
```

### 3. ViewModelErrorHandler
Base class and utilities for ViewModels with error handling.

**Usage:**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    private val _errorState = MutableStateFlow<ErrorInfo?>(null)
    val errorState: StateFlow<ErrorInfo?> = _errorState.asStateFlow()
    
    fun performOperation() {
        executeWithErrorHandling("执行操作") {
            repository.doSomething()
        }
    }
    
    private fun executeWithErrorHandling(
        context: String,
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _errorState.value = null
                operation()
            } catch (exception: Exception) {
                val errorInfo = errorHandler.handleError(exception, context = context)
                _errorState.value = errorInfo
            }
        }
    }
}
```

### 4. UI Components

#### ErrorSnackbar
Displays errors as snackbars with action buttons.

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val errorState by viewModel.errorState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    ErrorSnackbar(
        errorInfo = errorState,
        snackbarHostState = snackbarHostState,
        onDismiss = { viewModel.clearError() },
        onRetry = { viewModel.retryLastOperation() },
        onAction = { errorInfo ->
            // Handle specific error actions
        },
        scope = scope
    )
    
    // Your UI content
    Box {
        // Content...
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

#### ErrorDialog
Displays errors as dialogs for more complex scenarios.

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val errorState by viewModel.errorState.collectAsState()
    
    ErrorDialog(
        errorInfo = errorState,
        onDismiss = { viewModel.clearError() },
        onRetry = { viewModel.retryLastOperation() },
        onAction = { errorInfo ->
            errorHandler.executeErrorAction(errorInfo)
        }
    )
}
```

## Error Types

### Network Errors
- `UnknownHostException`: 无法连接到网络
- `ConnectException`: 网络连接超时
- `SocketTimeoutException`: 网络请求超时

### Storage Errors
- `ENOSPC`: 存储空间不足
- `Permission denied`: 存储权限被拒绝
- `FileNotFoundException`: 文件未找到

### Database Errors
- `SQLiteException` (corrupt): 数据库已损坏
- `SQLiteException` (disk I/O): 数据库读写错误
- General database errors: 数据库操作失败

### Other Error Types
- `SecurityException`: 权限不足
- `IllegalArgumentException`: 输入数据无效
- `OutOfMemoryError`: 内存不足
- `SerializationException`: 数据格式错误
- Image processing errors: 图片格式不支持

## Error Actions

The system supports automatic actions based on error type:

- `RETRY`: 重试操作
- `OPEN_SETTINGS`: 打开应用设置
- `OPEN_STORAGE_SETTINGS`: 打开存储设置
- `RESTORE_BACKUP`: 恢复备份
- `NONE`: 无自动操作

## Integration Examples

### Repository Integration
```kotlin
@Singleton
class ClothingRepositoryImpl @Inject constructor(
    private val dao: ClothingItemDao,
    private val errorHandler: RepositoryErrorHandler
) : ClothingRepository {
    
    override suspend fun insertClothing(clothing: ClothingItem): Long {
        return errorHandler.withDatabaseErrorHandling("insertClothing") {
            dao.insertClothingItem(clothing.toEntity())
        }
    }
    
    override fun getAllClothing(): Flow<List<ClothingItem>> {
        return dao.getAllClothingItems()
            .map { entities -> entities.map { it.toClothingItem() } }
            .withErrorHandling(errorHandler, "getAllClothing")
    }
}
```

### ViewModel Integration
```kotlin
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val repository: ClothingRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    private val _errorState = MutableStateFlow<ErrorInfo?>(null)
    val errorState: StateFlow<ErrorInfo?> = _errorState.asStateFlow()
    
    fun deleteClothingItem(item: ClothingItem) {
        executeWithErrorHandling("删除衣服项目") {
            repository.deleteClothing(item)
        }
    }
    
    private fun executeWithErrorHandling(
        context: String,
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _errorState.value = null
                operation()
            } catch (exception: Exception) {
                val errorInfo = errorHandler.handleError(exception, context = context)
                _errorState.value = errorInfo
            }
        }
    }
}
```

### Screen Integration
```kotlin
@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel = hiltViewModel()) {
    val errorState by viewModel.errorState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Error handling
    ErrorSnackbar(
        errorInfo = errorState,
        snackbarHostState = snackbarHostState,
        onDismiss = { viewModel.clearError() },
        onRetry = { viewModel.retryLastOperation() },
        scope = scope
    )
    
    Column {
        // Your UI content
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

## Best Practices

1. **Always use context**: Provide meaningful context strings for better debugging
2. **Handle errors at the right level**: Repository for data errors, ViewModel for business logic errors
3. **Provide user-friendly messages**: Use the built-in Chinese messages or provide custom ones
4. **Use appropriate UI components**: Snackbars for minor errors, dialogs for critical errors
5. **Implement retry logic**: Use the built-in retry mechanisms for recoverable errors
6. **Log errors properly**: The system automatically logs errors with context information

## Testing

The error handling system includes comprehensive unit tests:

```kotlin
@Test
fun `handleError should categorize network errors correctly`() {
    val error = errorHandler.handleError(UnknownHostException("Host not found"))
    assertEquals(ErrorType.NETWORK_ERROR, error.type)
    assertEquals("无法连接到网络", error.userMessage)
    assertTrue(error.isRecoverable)
}
```

Run tests with:
```bash
./gradlew test --tests "*ErrorHandlerTest*"
```