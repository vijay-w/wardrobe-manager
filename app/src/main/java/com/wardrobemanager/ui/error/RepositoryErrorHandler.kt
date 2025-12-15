package com.wardrobemanager.ui.error

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension functions for repositories to handle errors consistently
 */
@Singleton
class RepositoryErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    
    /**
     * Wraps a suspend function with error handling
     */
    suspend fun <T> safeCall(
        context: String,
        operation: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val errorInfo = errorHandler.handleError(e, context = context)
            Result.failure(e)
        }
    }
    
    /**
     * Wraps a Flow with error handling
     */
    fun <T> safeFlow(
        context: String,
        flowOperation: () -> Flow<T>
    ): Flow<T> {
        return flow {
            flowOperation().catch { e ->
                val errorInfo = errorHandler.handleError(e as Exception, context = context)
                throw e
            }.collect { value ->
                emit(value)
            }
        }
    }
    
    /**
     * Wraps database operations with specific error handling
     */
    suspend fun <T> safeDatabaseCall(
        context: String,
        operation: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val errorInfo = when (e) {
                is android.database.sqlite.SQLiteException -> {
                    errorHandler.handleError(
                        e, 
                        userMessage = "数据库操作失败",
                        context = context
                    )
                }
                else -> {
                    errorHandler.handleError(e, context = context)
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Wraps file operations with specific error handling
     */
    suspend fun <T> safeFileOperation(
        context: String,
        operation: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val errorInfo = when (e) {
                is java.io.IOException -> {
                    errorHandler.handleError(
                        e,
                        userMessage = errorHandler.getStorageErrorMessage(e),
                        context = context
                    )
                }
                else -> {
                    errorHandler.handleError(e, context = context)
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Wraps network operations with specific error handling
     */
    suspend fun <T> safeNetworkCall(
        context: String,
        operation: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            val errorInfo = when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException,
                is java.net.SocketTimeoutException -> {
                    errorHandler.handleError(
                        e,
                        userMessage = errorHandler.getNetworkErrorMessage(e),
                        context = context
                    )
                }
                else -> {
                    errorHandler.handleError(e, context = context)
                }
            }
            Result.failure(e)
        }
    }
}

/**
 * Extension functions for easier use in repositories
 */
suspend fun <T> RepositoryErrorHandler.withDatabaseErrorHandling(
    context: String,
    operation: suspend () -> T
): T {
    val result = safeDatabaseCall(context, operation)
    return result.getOrThrow()
}

suspend fun <T> RepositoryErrorHandler.withFileErrorHandling(
    context: String,
    operation: suspend () -> T
): T {
    val result = safeFileOperation(context, operation)
    return result.getOrThrow()
}

suspend fun <T> RepositoryErrorHandler.withNetworkErrorHandling(
    context: String,
    operation: suspend () -> T
): T {
    val result = safeNetworkCall(context, operation)
    return result.getOrThrow()
}

fun <T> Flow<T>.withErrorHandling(
    errorHandler: RepositoryErrorHandler,
    context: String
): Flow<T> {
    return this.catch { e ->
        errorHandler.errorHandler.handleError(e as Exception, context = context)
        throw e
    }
}