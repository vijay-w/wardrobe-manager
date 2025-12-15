package com.wardrobemanager.ui.addclothing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddClothingViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClothingUiState())
    val uiState: StateFlow<AddClothingUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddClothingEvent) {
        when (event) {
            is AddClothingEvent.ImageSourceDialogRequested -> {
                _uiState.update { it.copy(showImageSourceDialog = true) }
            }
            is AddClothingEvent.ImageSourceDialogDismissed -> {
                _uiState.update { it.copy(showImageSourceDialog = false) }
            }
            is AddClothingEvent.CameraRequested -> {
                _uiState.update { it.copy(showImageSourceDialog = false) }
                // Camera handling will be done in the UI
            }
            is AddClothingEvent.GalleryRequested -> {
                _uiState.update { it.copy(showImageSourceDialog = false) }
                // Gallery handling will be done in the UI
            }
            is AddClothingEvent.ImageSelected -> {
                handleImageSelection(event.uri)
            }
            is AddClothingEvent.NameChanged -> {
                _uiState.update { 
                    it.copy(
                        name = event.name,
                        nameError = if (event.name.isBlank()) "名称不能为空" else null
                    )
                }
            }
            is AddClothingEvent.CategoryChanged -> {
                _uiState.update { it.copy(category = event.category) }
            }
            is AddClothingEvent.RatingChanged -> {
                _uiState.update { 
                    it.copy(rating = event.rating.coerceIn(0f, 5f))
                }
            }
            is AddClothingEvent.PriceChanged -> {
                val priceError = validatePrice(event.price)
                _uiState.update { 
                    it.copy(
                        price = event.price,
                        priceError = priceError
                    )
                }
            }
            is AddClothingEvent.PurchaseLinkChanged -> {
                val linkError = validatePurchaseLink(event.link)
                _uiState.update { 
                    it.copy(
                        purchaseLink = event.link,
                        linkError = linkError
                    )
                }
            }
            is AddClothingEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.notes) }
            }
            is AddClothingEvent.SaveClothing -> {
                saveClothing()
            }
            is AddClothingEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun handleCameraImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageProcessing = true) }
            try {
                val imagePath = imageRepository.saveImageFromCamera(imageFile)
                _uiState.update { 
                    it.copy(
                        imagePath = imagePath,
                        isImageProcessing = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "保存图片失败: ${exception.message}",
                        isImageProcessing = false
                    )
                }
            }
        }
    }

    private fun handleImageSelection(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageProcessing = true) }
            try {
                val imagePath = imageRepository.saveImage(uri)
                _uiState.update { 
                    it.copy(
                        imageUri = uri,
                        imagePath = imagePath,
                        isImageProcessing = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "保存图片失败: ${exception.message}",
                        isImageProcessing = false
                    )
                }
            }
        }
    }

    private fun saveClothing() {
        val currentState = _uiState.value
        
        // Validate form
        val nameError = if (currentState.name.isBlank()) "名称不能为空" else null
        val priceError = validatePrice(currentState.price)
        val linkError = validatePurchaseLink(currentState.purchaseLink)
        val imageError = if (currentState.imagePath.isBlank()) "请选择图片" else null
        
        if (nameError != null || priceError != null || linkError != null || imageError != null) {
            _uiState.update { 
                it.copy(
                    nameError = nameError,
                    priceError = priceError,
                    linkError = linkError,
                    error = imageError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val clothingItem = ClothingItem(
                    name = currentState.name.trim(),
                    category = currentState.category,
                    imagePath = currentState.imagePath,
                    rating = currentState.rating,
                    price = if (currentState.price.isBlank()) null else currentState.price.toDoubleOrNull(),
                    purchaseLink = if (currentState.purchaseLink.isBlank()) null else currentState.purchaseLink.trim(),
                    notes = if (currentState.notes.isBlank()) null else currentState.notes.trim()
                )
                
                clothingRepository.insertClothing(clothingItem)
                
                // Reset form after successful save
                _uiState.value = AddClothingUiState()
                
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "保存失败: ${exception.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun validatePrice(price: String): String? {
        if (price.isBlank()) return null
        
        return try {
            val priceValue = price.toDouble()
            if (priceValue < 0) "价格不能为负数" else null
        } catch (e: NumberFormatException) {
            "请输入有效的价格"
        }
    }

    private fun validatePurchaseLink(link: String): String? {
        if (link.isBlank()) return null
        
        val urlPattern = Regex(
            "^(https?://)?" +
            "([\\w\\-]+\\.)+[\\w\\-]+" +
            "(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*)?$",
            RegexOption.IGNORE_CASE
        )
        
        return if (urlPattern.matches(link)) null else "请输入有效的链接格式"
    }

    fun resetForm() {
        _uiState.value = AddClothingUiState()
    }
}