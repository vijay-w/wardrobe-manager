package com.wardrobemanager.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    private val context: Context
) {
    companion object {
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        
        val STORAGE_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val STORAGE_PERMISSION_REQUEST_CODE = 1002
    }
    
    fun hasCameraPermission(): Boolean {
        return CAMERA_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasStoragePermission(): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasAllRequiredPermissions(): Boolean {
        return hasCameraPermission() && hasStoragePermission()
    }
    
    fun getCameraPermissions(): Array<String> = CAMERA_PERMISSIONS
    fun getStoragePermissions(): Array<String> = STORAGE_PERMISSIONS
    
    fun getAllRequiredPermissions(): Array<String> {
        return CAMERA_PERMISSIONS + STORAGE_PERMISSIONS
    }
}