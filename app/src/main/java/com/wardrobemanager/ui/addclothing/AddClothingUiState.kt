package com.wardrobemanager.ui.addclothing

import android.net.Uri
import com.wardrobemanager.data.model.ClothingCategory

data class AddClothingUiState(
    val name: String = "",
    val category: ClothingCategory = ClothingCategory.TOP,
    val imageUri: Uri? = null,
    val imagePath: String = "",
    val rating: Float = 0f,
    val price: String = "",
    val purchaseLink: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val priceError: String? = null,
    val linkError: String? = null,
    val showImageSourceDialog: Boolean = false,
    val isImageProcessing: Boolean = false
)

sealed class AddClothingEvent {
    object ImageSourceDialogRequested : AddClothingEvent()
    object ImageSourceDialogDismissed : AddClothingEvent()
    object CameraRequested : AddClothingEvent()
    object GalleryRequested : AddClothingEvent()
    data class ImageSelected(val uri: Uri) : AddClothingEvent()
    data class NameChanged(val name: String) : AddClothingEvent()
    data class CategoryChanged(val category: ClothingCategory) : AddClothingEvent()
    data class RatingChanged(val rating: Float) : AddClothingEvent()
    data class PriceChanged(val price: String) : AddClothingEvent()
    data class PurchaseLinkChanged(val link: String) : AddClothingEvent()
    data class NotesChanged(val notes: String) : AddClothingEvent()
    object SaveClothing : AddClothingEvent()
    object ClearError : AddClothingEvent()
}