package com.dasurv.ui.screen.pigment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.ui.util.parseHexSafe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorRecommendationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PigmentViewModel = hiltViewModel()
) {
    val recommendations by viewModel.desiredColorRecommendations.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    val presetColors = listOf(
        "#F4A7B9" to "Light Pink",
        "#E8728A" to "Medium Pink",
        "#CE5B78" to "Rose",
        "#C41E3A" to "Red",
        "#9E4B5E" to "Rosewood",
        "#D2691E" to "Coral",
        "#B784A7" to "Mauve",
        "#800020" to "Maroon"
    )
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(title = "Color Recommendation") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item {
                Text(
                    "Select Desired Lip Color",
                    style = MaterialTheme.typography.titleMedium,
                    color = M3OnSurface
                )
                Text(
                    "Choose the result color you want to achieve",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    presetColors.forEach { (hex, name) ->
                        ColorSwatch(
                            colorHex = hex,
                            label = name,
                            selected = selectedPreset == hex,
                            onClick = {
                                selectedPreset = hex
                                viewModel.getRecommendationsForDesiredColor(hex)
                            }
                        )
                    }
                }
            }

            if (recommendations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        "Recommended Pigments",
                        style = MaterialTheme.typography.titleMedium,
                        color = M3OnSurface
                    )
                }

                item {
                    M3ListCard {
                        recommendations.forEachIndexed { index, rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .padding(horizontal = spacing.lg, vertical = spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            remember(rec.pigment.colorHex) {
                                                parseHexSafe(rec.pigment.colorHex)
                                            }
                                        )
                                        .border(1.dp, M3Outline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(spacing.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        rec.pigment.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        rec.pigment.brand.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = M3OnSurfaceVariant
                                    )
                                    Text(
                                        rec.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = M3OnSurfaceVariant
                                    )
                                }
                                Text(
                                    "${(rec.matchScore * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = M3Primary
                                )
                            }
                            if (index < recommendations.lastIndex) {
                                M3ListDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
