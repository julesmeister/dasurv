package com.dasurv.ui.screen.pigment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.ui.component.ColorSwatch

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
                title = { Text("Pigment Catalogue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPigmentInventory) {
                        Icon(Icons.Default.Opacity, "Pigment Inventory")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRecommendation,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allBrands = listOf<PigmentBrand?>(null) + PigmentBrand.entries
                allBrands.forEach { brand ->
                    val isSelected = selectedBrand == brand
                    FilledTonalButton(
                        onClick = { viewModel.selectBrand(brand) },
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
                            text = brand?.displayName ?: "All",
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                "${pigments.size} colors",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pigment.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorSwatch(colorHex = pigment.colorHex, label = "")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(pigment.brand.displayName, style = MaterialTheme.typography.bodyMedium)
                        Text(pigment.colorHex, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (pigment.undertone.isNotBlank() || pigment.intensity.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text(pigment.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = onAddToInventory) {
                Icon(Icons.Default.Opacity, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
