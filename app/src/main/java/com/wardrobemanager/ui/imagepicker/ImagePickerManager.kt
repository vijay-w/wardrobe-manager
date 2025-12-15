package com.wardrobemanager.ui.imagepicker

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagePickerManager @Inject constructor(
    private val context: Context
) {
    
    fun createCropImageOptions(): CropImageOptions {
        return CropImageOptions().apply {
            imageSourceIncludeGallery = true
            imageSourceIncludeCamera = false // We handle camera separately
            guidelines = CropImageView.Guidelines.ON
            aspectRatioX = 1
            aspectRatioY = 1
            fixAspectRatio = true
            cropShape = CropImageView.CropShape.RECTANGLE
            autoZoomEnabled = true
            maxZoom = 8
            initialCropWindowPaddingRatio = 0.1f
            borderLineThickness = 3f
            borderCornerThickness = 2f
            borderCornerOffset = 5f
            borderCornerLength = 14f
            guidelinesThickness = 1f
            snapRadius = 3f
            touchRadius = 48f
            initialCropWindowRectangle = null
            initCropWindowSizePercent = 0.9f
            borderCornerColor = android.graphics.Color.WHITE
            borderLineColor = android.graphics.Color.WHITE
            guidelinesColor = android.graphics.Color.WHITE
            backgroundColor = android.graphics.Color.BLACK
            minCropWindowWidth = 40
            minCropWindowHeight = 40
            minCropResultWidth = 20
            minCropResultHeight = 20
            maxCropResultWidth = 99999
            maxCropResultHeight = 99999
            activityTitle = "裁剪图片"
            activityMenuIconColor = android.graphics.Color.WHITE
            outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
            outputCompressQuality = 90
            outputRequestWidth = 1024
            outputRequestHeight = 1024
            outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE
            noOutputImage = false
            initialRotation = 0
            allowRotation = true
            allowFlipping = true
            allowCounterRotation = true
            rotationDegrees = 90
            flipHorizontally = false
            flipVertically = false
            cropMenuCropButtonTitle = "裁剪"
            cropMenuCropButtonIcon = 0
        }
    }
    
    fun createCropImageContractOptions(sourceUri: Uri): CropImageContractOptions {
        return CropImageContractOptions(
            uri = sourceUri,
            cropImageOptions = createCropImageOptions()
        )
    }
    
    fun createPickVisualMediaRequest(): PickVisualMediaRequest {
        return PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    }
}