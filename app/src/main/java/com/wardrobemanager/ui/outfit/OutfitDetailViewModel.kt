package com.wardrobemanager.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutfitDetailViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothingRepository: ClothingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitDetailUiState())
    val uiState: StateFlow<OutfitDetailUiState> = _uiState.asStateFlow()

    private val _availableClothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val availableClothingItems: StateFlow<List<ClothingItem>> = _availableClothingItems.asStateFlow()

    fun loadOutfit(outfitId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val outfit = outfitRepository.getOutfitById(outfitId)
                _uiState.update { 
                    it.copy(
                        outfit = outfit,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadAvailableClothingItems() {
        viewModelScope.launch {
            clothingRepository.getAllClothing().collect { items ->
                _availableClothingItems.value = items
            }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
        loadAvailableClothingItems()
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun updateOutfitName(name: String) {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedOutfit = currentOutfit.copy(name = name)
        _uiState.update { it.copy(outfit = updatedOutfit) }
    }

    fun updateOutfitDescription(description: String) {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedOutfit = currentOutfit.copy(description = description)
        _uiState.update { it.copy(outfit = updatedOutfit) }
    }

    fun updateOutfitRating(rating: Float) {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedOutfit = currentOutfit.copy(rating = rating)
        _uiState.update { it.copy(outfit = updatedOutfit) }
    }

    fun addClothingItemToOutfit(clothingItem: ClothingItem) {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedClothingItems = currentOutfit.clothingItems + clothingItem
        val updatedOutfit = currentOutfit.copy(clothingItems = updatedClothingItems)
        _uiState.update { it.copy(outfit = updatedOutfit) }
    }

    fun removeClothingItemFromOutfit(clothingItem: ClothingItem) {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedClothingItems = currentOutfit.clothingItems.filter { it.id != clothingItem.id }
        val updatedOutfit = currentOutfit.copy(clothingItems = updatedClothingItems)
        _uiState.update { it.copy(outfit = updatedOutfit) }
    }

    fun saveOutfit() {
        val currentOutfit = _uiState.value.outfit ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                outfitRepository.updateOutfit(currentOutfit)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isEditing = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteOutfit() {
        val currentOutfit = _uiState.value.outfit ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                outfitRepository.deleteOutfit(currentOutfit)
                // Navigation should be handled by the UI
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun markAsWorn() {
        val currentOutfit = _uiState.value.outfit ?: return
        val updatedOutfit = currentOutfit.copy(lastWorn = System.currentTimeMillis())
        
        viewModelScope.launch {
            try {
                outfitRepository.updateOutfit(updatedOutfit)
                _uiState.update { it.copy(outfit = updatedOutfit) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}