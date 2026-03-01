package com.dasurv.ui.screen.pigment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigmentCatalogueScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecommendation: () -> Unit,
    onNavigateToPigmentInventory: () -> Unit = {},
    onNavigateToAddStock: (name: String, brand: String, colorHex: String) -> Unit = { _, _, _ -> },
    viewModel: PigmentViewModel = hiltViewModel()
) {
    val pigments by viewModel.pigments.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = DasurvTheme.spacing

    var selectedPigment by remember { mutableStateOf<Pigment?>(null) }

    if (selectedPigment != null) {
        PigmentDetailDialog(
            pigment = selectedPigment!!,
            onDismiss = { selectedPigment = null },
            onAddToInventory = {
                val p = selectedPigment!!
                selectedPigment = null
                onNavigateToAddStock(p.name, p.brand.displayName, p.colorHex)
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(title = "Pigment Catalogue") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = onNavigateToPigmentInventory) {
                        Icon(Icons.Default.Opacity, "Pigment Inventory", tint = M3OnSurface)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRecommendation,
                containerColor = M3Primary,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Text("Color Match")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Brand filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                val allBrands = listOf<PigmentBrand?>(null) + PigmentBrand.entries
                allBrands.forEach { brand ->
                    val isSelected = selectedBrand == brand
                    FilledTonalButton(
                        onClick = { viewModel.selectBrand(brand) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected) M3PrimaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (isSelected) M3Primary
                            else M3OnSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm)
                    ) {
                        Text(
                            text = brand?.displayName ?: "All",
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                "${pigments.size} colors",
                modifier = Modifier.padding(horizontal = spacing.lg),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                items(pigments) { pigment ->
                    ColorSwatch(
                        colorHex = pigment.colorHex,
                        label = pigment.name,
                        onClick = { selectedPigment = pigment }
                    )
                }
            }
        }
    }
}

@Composable
private fun PigmentDetailDialog(
    pigment: Pigment,
    onDismiss: () -> Unit,
    onAddToInventory: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pigment.name, color = M3OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorSwatch(colorHex = pigment.colorHex, label = "")
                    Spacer(modifier = Modifier.width(spacing.md))
                    Column {
                        Text(
                            pigment.brand.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = M3OnSurface
                        )
                        Text(
                            pigment.colorHex,
                            style = MaterialTheme.typography.bodySmall,
                            color = M3OnSurfaceVariant
                        )
                    }
                }
                if (pigment.undertone.isNotBlank() || pigment.intensity.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        if (pigment.undertone.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(pigment.undertone.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        if (pigment.intensity.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(pigment.intensity.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
                if (pigment.description.isNotBlank()) {
                    Text(
                        pigment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddToInventory,
                colors = ButtonDefaults.buttonColors(containerColor = M3Primary)
            ) {
                Icon(Icons.Default.Opacity, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = M3OnSurfaceVariant) }
        }
    )
}
