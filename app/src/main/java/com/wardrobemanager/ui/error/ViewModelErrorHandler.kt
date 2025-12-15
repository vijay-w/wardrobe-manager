package com.wardrobemanager.ui.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base class for ViewModels with integrated error handling
 */
abstract class BaseViewModel @Inject constructor(
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    private val _errorState = MutableStateFlow<ErrorInfo?>(null)
    val errorState: StateFlow<ErrorInfo?> = _errorState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    protected val errorHandlingScope = CoroutineExceptionHandler { _, exception ->
        handleError(exception, "ViewModel operation failed")
    }
    
    protected fun handleError(
        throwable: Throwable,
        userMessage: String? = null,
        context: String? = null
    ) {
        val errorInfo = errorHandler.handleError(
            throwable as Exception,
            userMessage = userMessage,
            context = context
        )
        _errorState.value = errorInfo
    }
    
    protected fun clearError() {
        _errorState.value = null
    }
    
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * Execute an operation with error handling and loading state
     */
    protected fun executeWithErrorHandling(
        context: String? = null,
        showLoading: Boolean = true,
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch(errorHandlingScope) {
            try {
                if (showLoading) setLoading(true)
                clearError()
                operation()
            } catch (e: Exception) {
                handleError(e, context = context)
            } finally {
                if (showLoading) setLoading(false)
            }
        }
    }
    
    /**
     * Execute an operation with retry capability
     */
    protected fun executeWithRetry(
        context: String? = null,
        maxRetries: Int = 3,
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch(errorHandlingScope) {
            var attempts = 0
            var lastException: Exception? = null
            
            while (attempts < maxRetries) {
                try {
                    setLoading(true)
                    clearError()
                    operation()
                    return@launch
                } catch (e: Exception) {
                    attempts++
                    lastException = e
                    
                    val errorInfo = errorHandler.handleError(e, context = context)
                    
                    if (!errorHandler.shouldRetryOperation(errorInfo) || attempts >= maxRetries) {
                        _errorState.value = errorInfo
                        return@launch
                    }
                    
                    // Wait before retry
                    kotlinx.coroutines.delay(1000L * attempts)
                }
            }
            
            lastException?.let { 
                handleError(it, context = context)
            }
        }.invokeOnCompletion {
            setLoading(false)
        }
    }
    
    /**
     * Retry the last failed operation
     */
    fun retryLastOperation() {
        // This would need to be implemented by subclasses
        // to store and retry the last operation
    }
}

/**
 * Extension functions for ViewModels that don't extend BaseViewModel
 */
class ViewModelErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    
    fun createErrorState(): MutableStateFlow<ErrorInfo?> {
        return MutableStateFlow(null)
    }
    
    fun handleViewModelError(
        errorState: MutableStateFlow<ErrorInfo?>,
        throwable: Throwable,
        userMessage: String? = null,
        context: String? = null
    ) {
        val errorInfo = errorHandler.handleError(
            throwable as Exception,
            userMessage = userMessage,
            context = context
        )
        errorState.value = errorInfo
    }
    
    fun clearViewModelError(errorState: MutableStateFlow<ErrorInfo?>) {
        errorState.value = null
    }
}

/**
 * Extension function for ViewModels to handle operations safely
 */
fun ViewModel.safeViewModelCall(
    errorHandler: ErrorHandler,
    errorState: MutableStateFlow<ErrorInfo?>,
    context: String? = null,
    operation: suspend () -> Unit
) {
    viewModelScope.launch {
        try {
            errorState.value = null
            operation()
        } catch (e: Exception) {
            val errorInfo = errorHandler.handleError(e, context = context)
            errorState.value = errorInfo
        }
    }
}