package com.dasurv.ui.screen.pigment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.ColorSwatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorRecommendationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PigmentViewModel = hiltViewModel()
) {
    val recommendations by viewModel.desiredColorRecommendations.collectAsStateWithLifecycle()

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
                title = { Text("Color Recommendation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Select Desired Lip Color",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Choose the result color you want to achieve",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Recommended Pigments",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(recommendations) { rec ->
                    Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(rec.pigment.colorHex))
                                        } catch (e: Exception) {
                                            Color.Gray
                                        }
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.pigment.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    rec.pigment.brand.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    rec.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${(rec.matchScore * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
