package com.wardrobemanager.ui.search

import com.wardrobemanager.data.model.ClothingItem
import com.wardrobemanager.data.model.Outfit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchManager @Inject constructor() {
    
    fun highlightSearchTerm(text: String, searchTerm: String): List<SearchHighlight> {
        if (searchTerm.isBlank()) {
            return listOf(SearchHighlight(text, false))
        }
        
        val highlights = mutableListOf<SearchHighlight>()
        val lowerText = text.lowercase()
        val lowerSearchTerm = searchTerm.lowercase()
        
        var startIndex = 0
        var foundIndex = lowerText.indexOf(lowerSearchTerm, startIndex)
        
        while (foundIndex != -1) {
            // Add non-highlighted text before the match
            if (foundIndex > startIndex) {
                highlights.add(
                    SearchHighlight(
                        text.substring(startIndex, foundIndex),
                        false
                    )
                )
            }
            
            // Add highlighted match
            highlights.add(
                SearchHighlight(
                    text.substring(foundIndex, foundIndex + searchTerm.length),
                    true
                )
            )
            
            startIndex = foundIndex + searchTerm.length
            foundIndex = lowerText.indexOf(lowerSearchTerm, startIndex)
        }
        
        // Add remaining non-highlighted text
        if (startIndex < text.length) {
            highlights.add(
                SearchHighlight(
                    text.substring(startIndex),
                    false
                )
            )
        }
        
        return highlights
    }
    
    fun getSearchSuggestions(
        query: String,
        clothingItems: List<ClothingItem>,
        outfits: List<Outfit>
    ): List<SearchSuggestion> {
        if (query.length < 2) return emptyList()
        
        val suggestions = mutableSetOf<SearchSuggestion>()
        val lowerQuery = query.lowercase()
        
        // Add clothing item name suggestions
        clothingItems.forEach { item ->
            if (item.name.lowercase().contains(lowerQuery)) {
                suggestions.add(
                    SearchSuggestion(
                        text = item.name,
                        type = SearchSuggestionType.CLOTHING_NAME,
                        count = 1
                    )
                )
            }
        }
        
        // Add category suggestions
        clothingItems.groupBy { it.category }
            .forEach { (category, items) ->
                if (category.displayName.lowercase().contains(lowerQuery)) {
                    suggestions.add(
                        SearchSuggestion(
                            text = category.displayName,
                            type = SearchSuggestionType.CATEGORY,
                            count = items.size
                        )
                    )
                }
            }
        
        // Add outfit name suggestions
        outfits.forEach { outfit ->
            if (outfit.name.lowercase().contains(lowerQuery)) {
                suggestions.add(
                    SearchSuggestion(
                        text = outfit.name,
                        type = SearchSuggestionType.OUTFIT_NAME,
                        count = 1
                    )
                )
            }
        }
        
        return suggestions.take(5) // Limit to 5 suggestions
    }
    
    fun filterClothingItems(
        items: List<ClothingItem>,
        query: String
    ): List<ClothingItem> {
        if (query.isBlank()) return items
        
        val lowerQuery = query.lowercase()
        return items.filter { item ->
            item.name.lowercase().contains(lowerQuery) ||
            item.category.displayName.lowercase().contains(lowerQuery) ||
            item.notes?.lowercase()?.contains(lowerQuery) == true
        }
    }
    
    fun filterOutfits(
        outfits: List<Outfit>,
        query: String
    ): List<Outfit> {
        if (query.isBlank()) return outfits
        
        val lowerQuery = query.lowercase()
        return outfits.filter { outfit ->
            outfit.name.lowercase().contains(lowerQuery) ||
            outfit.description?.lowercase()?.contains(lowerQuery) == true ||
            outfit.clothingItems.any { item ->
                item.name.lowercase().contains(lowerQuery)
            }
        }
    }
}

data class SearchHighlight(
    val text: String,
    val isHighlighted: Boolean
)

data class SearchSuggestion(
    val text: String,
    val type: SearchSuggestionType,
    val count: Int
)

enum class SearchSuggestionType {
    CLOTHING_NAME,
    CATEGORY,
    OUTFIT_NAME
}