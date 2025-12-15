package com.wardrobemanager.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.model.ClothingCategory
import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.ui.error.ErrorHandler
import com.wardrobemanager.ui.error.ErrorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository,
    private val imagePreloader: com.wardrobemanager.ui.imagepicker.ImagePreloader,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    private val _errorState = MutableStateFlow<ErrorInfo?>(null)
    val errorState: StateFlow<ErrorInfo?> = _errorState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val filterOptions = MutableStateFlow(FilterOptions())

    init {
        // Combine search and filter to get clothing items
        combine(
            searchQuery.debounce(300), // Debounce search input
            filterOptions
        ) { query, filter ->
            Pair(query, filter)
        }.flatMapLatest { (query, filter) ->
            if (query.isBlank()) {
                // Use filtered results when no search query
                clothingRepository.getFilteredClothing(
                    category = filter.category,
                    minRating = filter.minRating,
                    maxRating = filter.maxRating,
                    minPrice = filter.minPrice,
                    maxPrice = filter.maxPrice,
                    sortByRating = filter.sortByRating
                )
            } else {
                // Use search results and apply additional filtering
                clothingRepository.searchClothing(query).map { items ->
                    items.filter { item ->
                        (filter.category == null || item.category == filter.category) &&
                        (filter.minRating == null || item.rating >= filter.minRating) &&
                        (filter.maxRating == null || item.rating <= filter.maxRating) &&
                        (filter.minPrice == null || (item.price ?: 0.0) >= filter.minPrice) &&
                        (filter.maxPrice == null || (item.price ?: Double.MAX_VALUE) <= filter.maxPrice)
                    }.let { filteredItems ->
                        if (filter.sortByRating) {
                            filteredItems.sortedByDescending { it.rating }
                        } else {
                            filteredItems.sortedByDescending { it.createdAt }
                        }
                    }
                }
            }
        }.catch { exception ->
            val errorInfo = errorHandler.handleError(
                exception as Exception,
                userMessage = "加载衣橱数据失败",
                context = "WardrobeViewModel.loadClothingItems"
            )
            _errorState.value = errorInfo
            _uiState.update { it.copy(isLoading = false) }
        }.onStart {
            _uiState.update { it.copy(isLoading = true) }
        }.onEach { items ->
            _uiState.update { 
                it.copy(
                    clothingItems = items,
                    isLoading = false
                )
            }
            _errorState.value = null
            
            // Preload images for better performance
            val imagePaths = items.map { it.imagePath }
            imagePreloader.preloadClothingImages(imagePaths)
        }.launchIn(viewModelScope)

        // Update UI state when search query changes
        searchQuery.onEach { query ->
            _uiState.update { it.copy(searchQuery = query) }
        }.launchIn(viewModelScope)

        // Update UI state when filter options change
        filterOptions.onEach { filter ->
            _uiState.update { 
                it.copy(
                    selectedCategory = filter.category,
                    minRating = filter.minRating,
                    maxRating = filter.maxRating,
                    minPrice = filter.minPrice,
                    maxPrice = filter.maxPrice,
                    sortByRating = filter.sortByRating
                )
            }
        }.launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateCategory(category: ClothingCategory?) {
        filterOptions.update { it.copy(category = category) }
    }

    fun updateRatingFilter(minRating: Float?, maxRating: Float?) {
        filterOptions.update { 
            it.copy(minRating = minRating, maxRating = maxRating) 
        }
    }

    fun updatePriceFilter(minPrice: Double?, maxPrice: Double?) {
        filterOptions.update { 
            it.copy(minPrice = minPrice, maxPrice = maxPrice) 
        }
    }

    fun toggleSortByRating() {
        filterOptions.update { it.copy(sortByRating = !it.sortByRating) }
    }

    fun clearFilters() {
        filterOptions.value = FilterOptions()
        searchQuery.value = ""
    }

    fun toggleFilterExpansion() {
        _uiState.update { it.copy(isFilterExpanded = !it.isFilterExpanded) }
    }

    fun deleteClothingItem(clothingItem: ClothingItem) {
        executeWithErrorHandling("删除衣服项目") {
            clothingRepository.deleteClothing(clothingItem)
        }
    }

    fun updateClothingRating(clothingItem: ClothingItem, newRating: Float) {
        executeWithErrorHandling("更新衣服评分") {
            val updatedItem = clothingItem.copy(rating = newRating)
            clothingRepository.updateClothing(updatedItem)
        }
    }

    fun markAsWorn(clothingItem: ClothingItem) {
        executeWithErrorHandling("标记为已穿") {
            val updatedItem = clothingItem.copy(lastWorn = System.currentTimeMillis())
            clothingRepository.updateClothing(updatedItem)
        }
    }

    fun retryLastOperation() {
        // This could be enhanced to store and retry the last failed operation
        // For now, we'll just clear the error
        clearError()
    }

    fun clearError() {
        _errorState.value = null
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
                val errorInfo = errorHandler.handleError(
                    exception,
                    context = "WardrobeViewModel.$context"
                )
                _errorState.value = errorInfo
            }
        }
    }
}