package com.wardrobemanager.ui.error

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    private lateinit var errorHandler: ErrorHandler
    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        errorHandler = ErrorHandler(mockContext)
    }

    @Test
    fun `handleError should categorize network errors correctly`() {
        // Test UnknownHostException
        val unknownHostError = errorHandler.handleError(UnknownHostException("Host not found"))
        assertEquals(ErrorType.NETWORK_ERROR, unknownHostError.type)
        assertEquals("无法连接到网络", unknownHostError.userMessage)
        assertTrue(unknownHostError.isRecoverable)
        assertEquals(ErrorActionType.RETRY, unknownHostError.actionType)

        // Test ConnectException
        val connectError = errorHandler.handleError(ConnectException("Connection failed"))
        assertEquals(ErrorType.NETWORK_ERROR, connectError.type)
        assertEquals("网络连接超时", connectError.userMessage)
        assertTrue(connectError.isRecoverable)

        // Test SocketTimeoutException
        val timeoutError = errorHandler.handleError(SocketTimeoutException("Timeout"))
        assertEquals(ErrorType.NETWORK_ERROR, timeoutError.type)
        assertEquals("网络请求超时", timeoutError.userMessage)
        assertTrue(timeoutError.isRecoverable)
    }

    @Test
    fun `handleError should categorize storage errors correctly`() {
        // Test storage space error
        val noSpaceError = errorHandler.handleError(IOException("ENOSPC: No space left on device"))
        assertEquals(ErrorType.STORAGE_ERROR, noSpaceError.type)
        assertEquals("存储空间不足", noSpaceError.userMessage)
        assertTrue(noSpaceError.isRecoverable)
        assertEquals(ErrorActionType.OPEN_SETTINGS, noSpaceError.actionType)

        // Test permission error
        val permissionError = errorHandler.handleError(IOException("Permission denied"))
        assertEquals(ErrorType.PERMISSION_ERROR, permissionError.type)
        assertEquals("存储权限被拒绝", permissionError.userMessage)
        assertTrue(permissionError.isRecoverable)

        // Test file not found
        val fileNotFoundError = errorHandler.handleError(FileNotFoundException("File not found"))
        assertEquals(ErrorType.IO_ERROR, fileNotFoundError.type)
        assertEquals("文件未找到", fileNotFoundError.userMessage)
        assertTrue(fileNotFoundError.isRecoverable)
    }

    @Test
    fun `handleError should categorize database errors correctly`() {
        // Test database corruption
        val corruptError = errorHandler.handleError(
            android.database.sqlite.SQLiteException("database disk image is malformed (code 11)")
        )
        assertEquals(ErrorType.DATABASE_ERROR, corruptError.type)
        assertEquals("数据库已损坏", corruptError.userMessage)
        assertTrue(corruptError.isRecoverable)
        assertEquals(ErrorActionType.RESTORE_BACKUP, corruptError.actionType)

        // Test database I/O error
        val ioError = errorHandler.handleError(
            android.database.sqlite.SQLiteException("disk I/O error (code 10)")
        )
        assertEquals(ErrorType.DATABASE_ERROR, ioError.type)
        assertEquals("数据库读写错误", ioError.userMessage)
        assertTrue(ioError.isRecoverable)
    }

    @Test
    fun `handleError should include context information`() {
        val context = "TestOperation"
        val error = errorHandler.handleError(
            IOException("Test error"),
            context = context
        )
        assertEquals(context, error.context)
    }

    @Test
    fun `shouldRetryOperation should return correct values`() {
        val networkError = ErrorInfo(
            type = ErrorType.NETWORK_ERROR,
            userMessage = "Network error",
            technicalMessage = "Technical message",
            isRecoverable = true,
            suggestedAction = "Retry"
        )
        assertTrue(errorHandler.shouldRetryOperation(networkError))

        val memoryError = ErrorInfo(
            type = ErrorType.MEMORY_ERROR,
            userMessage = "Memory error",
            technicalMessage = "Technical message",
            isRecoverable = false,
            suggestedAction = "Restart"
        )
        assertFalse(errorHandler.shouldRetryOperation(memoryError))
    }

    @Test
    fun `isDataCorruption should detect corruption correctly`() {
        assertTrue(errorHandler.isDataCorruption(
            android.database.sqlite.SQLiteException("corrupt")
        ))
        assertTrue(errorHandler.isDataCorruption(
            kotlinx.serialization.SerializationException("Invalid format")
        ))
        assertTrue(errorHandler.isDataCorruption(
            IOException("File is corrupt")
        ))
        assertFalse(errorHandler.isDataCorruption(
            IOException("Regular IO error")
        ))
    }

    @Test
    fun `getNetworkErrorMessage should return appropriate messages`() {
        assertEquals(
            "无法连接到服务器，请检查网络连接",
            errorHandler.getNetworkErrorMessage(UnknownHostException())
        )
        assertEquals(
            "连接超时，请检查网络或稍后重试",
            errorHandler.getNetworkErrorMessage(ConnectException())
        )
        assertEquals(
            "网络请求超时，请稍后重试",
            errorHandler.getNetworkErrorMessage(SocketTimeoutException())
        )
        assertEquals(
            "网络错误，请检查连接",
            errorHandler.getNetworkErrorMessage(RuntimeException())
        )
    }

    @Test
    fun `getStorageErrorMessage should return appropriate messages`() {
        assertEquals(
            "存储空间不足，请清理后重试",
            errorHandler.getStorageErrorMessage(IOException("ENOSPC"))
        )
        assertEquals(
            "存储权限被拒绝，请在设置中授权",
            errorHandler.getStorageErrorMessage(IOException("Permission denied"))
        )
        assertEquals(
            "文件未找到，请重新选择",
            errorHandler.getStorageErrorMessage(FileNotFoundException())
        )
        assertEquals(
            "存储操作失败，请重试",
            errorHandler.getStorageErrorMessage(IOException("Other error"))
        )
    }
}