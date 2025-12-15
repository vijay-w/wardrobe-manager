package com.wardrobemanager.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    enabled: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            },
            trailingIcon = {
                Row {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除"
                            )
                        }
                    }
                }
            },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isExpanded = focusState.isFocused && suggestions.isNotEmpty()
                }
        )
        
        // Suggestions dropdown
        if (isExpanded && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionItem(
                            suggestion = suggestion,
                            query = query,
                            onClick = {
                                onSuggestionClick(suggestion)
                                isExpanded = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchManager: SearchManager = hiltViewModel<SearchBarViewModel>().searchManager
) {
    val highlights = searchManager.highlightSearchTerm(suggestion.text, query)
    
    ListItem(
        headlineContent = {
            Text(
                text = buildAnnotatedString {
                    highlights.forEach { highlight ->
                        if (highlight.isHighlighted) {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                append(highlight.text)
                            }
                        } else {
                            append(highlight.text)
                        }
                    }
                }
            )
        },
        supportingContent = {
            Text(
                text = when (suggestion.type) {
                    SearchSuggestionType.CLOTHING_NAME -> "衣服"
                    SearchSuggestionType.CATEGORY -> "分类 (${suggestion.count} 件)"
                    SearchSuggestionType.OUTFIT_NAME -> "穿搭"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = when (suggestion.type) {
                    SearchSuggestionType.CLOTHING_NAME -> Icons.Default.Checkroom
                    SearchSuggestionType.CATEGORY -> Icons.Default.Category
                    SearchSuggestionType.OUTFIT_NAME -> Icons.Default.Style
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
fun SearchBarViewModel(): SearchBarViewModel = hiltViewModel()

class SearchBarViewModel @javax.inject.Inject constructor(
    val searchManager: SearchManager
) : androidx.lifecycle.ViewModel()