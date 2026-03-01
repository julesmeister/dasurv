package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.Pigment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
internal fun LazyListScope.pigmentSourcePicker(
    isCustom: Boolean,
    onSetCustom: (Boolean) -> Unit,
    pigmentName: String,
    pigmentBrand: String,
    colorHex: String,
    onPigmentNameChange: (String) -> Unit,
    onPigmentBrandChange: (String) -> Unit,
    onColorHexChange: (String) -> Unit,
    allPigments: List<Pigment>,
    onPigmentSelected: (Pigment) -> Unit
) {
    // Toggle: From Catalog / Custom
    item {
        val spacing = DasurvTheme.spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            FilterChip(
                selected = !isCustom,
                onClick = { onSetCustom(false) },
                label = { Text("From Catalog") }
            )
            FilterChip(
                selected = isCustom,
                onClick = { onSetCustom(true) },
                label = { Text("Custom") }
            )
        }
    }

    if (!isCustom) {
        // Catalog mode: pigment picker dropdown
        item {
            val spacing = DasurvTheme.spacing
            var catalogExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = catalogExpanded,
                onExpandedChange = { catalogExpanded = it }
            ) {
                DasurvTextField(
                    value = if (pigmentName.isNotBlank()) "$pigmentName ($pigmentBrand)" else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Pigment") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catalogExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = catalogExpanded,
                    onDismissRequest = { catalogExpanded = false },
                    scrollState = rememberScrollState(),
                    shadowElevation = 0.dp
                ) {
                    allPigments.forEach { pigment ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ColorSwatch(colorHex = pigment.colorHex, label = "")
                                    Spacer(modifier = Modifier.width(spacing.sm))
                                    Column {
                                        Text(pigment.name, color = M3OnSurface)
                                        Text(
                                            pigment.brand.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = M3OnSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onPigmentSelected(pigment)
                                catalogExpanded = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Custom mode: manual text fields
        item {
            DasurvTextField(
                value = pigmentName,
                onValueChange = onPigmentNameChange,
                label = { Text("Pigment Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            DasurvTextField(
                value = pigmentBrand,
                onValueChange = onPigmentBrandChange,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            DasurvTextField(
                value = colorHex,
                onValueChange = onColorHexChange,
                label = { Text("Color Hex (e.g. #FF6B6B)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
