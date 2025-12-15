package com.wardrobemanager.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardrobemanager.data.model.Outfit
import com.wardrobemanager.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class OutfitListViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitListUiState())
    val uiState: StateFlow<OutfitListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val ratingFilter = MutableStateFlow<Pair<Float?, Float?>>(null to null)
    private val sortByRating = MutableStateFlow(false)

    init {
        // Combine search, filter, and sort options
        combine(
            searchQuery.debounce(300),
            ratingFilter,
            sortByRating
        ) { query, (minRating, maxRating), sortRating ->
            Triple(query, minRating to maxRating, sortRating)
        }.flatMapLatest { (query, ratingRange, sortRating) ->
            if (query.isBlank()) {
                outfitRepository.getFilteredOutfits(
                    minRating = ratingRange.first,
                    maxRating = ratingRange.second,
                    sortByRating = sortRating
                )
            } else {
                outfitRepository.searchOutfits(query).map { outfits ->
                    outfits.filter { outfit ->
                        (ratingRange.first == null || outfit.rating >= ratingRange.first) &&
                        (ratingRange.second == null || outfit.rating <= ratingRange.second)
                    }.let { filteredOutfits ->
                        if (sortRating) {
                            filteredOutfits.sortedByDescending { it.rating }
                        } else {
                            filteredOutfits.sortedByDescending { it.createdAt }
                        }
                    }
                }
            }
        }.catch { exception ->
            _uiState.update { it.copy(error = exception.message, isLoading = false) }
        }.onStart {
            _uiState.update { it.copy(isLoading = true) }
        }.onEach { outfits ->
            _uiState.update { 
                it.copy(
                    outfits = outfits,
                    isLoading = false,
                    error = null
                )
            }
        }.launchIn(viewModelScope)

        // Update UI state when parameters change
        searchQuery.onEach { query ->
            _uiState.update { it.copy(searchQuery = query) }
        }.launchIn(viewModelScope)

        ratingFilter.onEach { (minRating, maxRating) ->
            _uiState.update { 
                it.copy(minRating = minRating, maxRating = maxRating) 
            }
        }.launchIn(viewModelScope)

        sortByRating.onEach { sortRating ->
            _uiState.update { it.copy(sortByRating = sortRating) }
        }.launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateRatingFilter(minRating: Float?, maxRating: Float?) {
        ratingFilter.value = minRating to maxRating
    }

    fun toggleSortByRating() {
        sortByRating.update { !it }
    }

    fun clearFilters() {
        searchQuery.value = ""
        ratingFilter.value = null to null
        sortByRating.value = false
    }

    fun deleteOutfit(outfit: Outfit) {
        viewModelScope.launch {
            try {
                outfitRepository.deleteOutfit(outfit)
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message) }
            }
        }
    }

    fun updateOutfitRating(outfit: Outfit, newRating: Float) {
        viewModelScope.launch {
            try {
                val updatedOutfit = outfit.copy(rating = newRating)
                outfitRepository.updateOutfit(updatedOutfit)
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message) }
            }
        }
    }

    fun markOutfitAsWorn(outfit: Outfit) {
        viewModelScope.launch {
            try {
                val updatedOutfit = outfit.copy(lastWorn = System.currentTimeMillis())
                outfitRepository.updateOutfit(updatedOutfit)
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}