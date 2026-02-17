package com.dasurv.ui.screen.tryon

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.ui.component.ShimmerSwatchGrid

@Composable
internal fun PigmentFilterPanel(
    selectedBrand: PigmentBrand?,
    onSelectBrand: (PigmentBrand?) -> Unit,
    showFavoritesOnly: Boolean,
    onToggleFavorites: () -> Unit,
    isLoading: Boolean,
    pigments: List<Pigment>,
    selectedPigment: Pigment?,
    isFavorite: (Pigment) -> Boolean,
    onSelectPigment: (Pigment) -> Unit,
    onToggleFavorite: (Pigment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Brand filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allBrands = listOf<PigmentBrand?>(null) + PigmentBrand.entries
            allBrands.forEach { brand ->
                val isSelected = selectedBrand == brand
                FilledTonalButton(
                    onClick = { onSelectBrand(brand) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (brand) {
                            null -> "All"
                            PigmentBrand.PERMABLEND_LUXE -> "LUXE"
                            PigmentBrand.PERMABLEND_ORIGINAL -> "Original"
                            PigmentBrand.EVENFLO -> "Evenflo"
                            PigmentBrand.TRUNM -> "TRUNM"
                        },
                        maxLines = 1
                    )
                }
            }
            // Favorites toggle
            FilledTonalButton(
                onClick = onToggleFavorites,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (showFavoritesOnly)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (showFavoritesOnly)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AnimatedContent(
                    targetState = showFavoritesOnly,
                    transitionSpec = { scaleIn(spring()) togetherWith scaleOut(spring()) },
                    label = "fav_icon"
                ) { isFav ->
                    Icon(
                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text("Favorites", maxLines = 1)
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )

        // Pigment grid with shimmer loading & AnimatedContent
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            modifier = Modifier.weight(1f),
            label = "grid_content"
        ) { loading ->
            if (loading) {
                ShimmerSwatchGrid(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (pigments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showFavoritesOnly) "No favorites yet\nTap the heart to add some!"
                        else "No pigments found",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(pigments, key = { "${it.name}|${it.brand.displayName}" }) { pigment ->
                        PigmentSwatchItem(
                            pigment = pigment,
                            isSelected = selectedPigment == pigment,
                            isFavorite = isFavorite(pigment),
                            onTap = { onSelectPigment(pigment) },
                            onToggleFavorite = { onToggleFavorite(pigment) }
                        )
                    }
                }
            }
        }
    }
}
