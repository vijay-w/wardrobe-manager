package com.wardrobemanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 16.dp,
    starColor: Color = Color(0xFFFFD700), // Gold color
    emptyStarColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            val isFilled = starIndex <= rating
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Star $starIndex",
                tint = if (isFilled) starColor else emptyStarColor,
                modifier = Modifier
                    .size(starSize)
                    .clickable(enabled = enabled) {
                        onRatingChange(starIndex.toFloat())
                    }
            )
        }
    }
}

@Composable
fun ReadOnlyRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 16.dp,
    starColor: Color = Color(0xFFFFD700),
    emptyStarColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            val isFilled = starIndex <= rating
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Star $starIndex",
                tint = if (isFilled) starColor else emptyStarColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}