package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.model.Pigment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

/**
 * Reusable "From Catalog / Custom" toggle row.
 */
@Composable
internal fun CatalogCustomToggle(
    isCustom: Boolean,
    onSetCustom: (Boolean) -> Unit
) {
    val spacing = DasurvTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        DasurvFilterChip(
            label = "From Catalog",
            selected = !isCustom,
            onClick = { onSetCustom(false) }
        )
        DasurvFilterChip(
            label = "Custom",
            selected = isCustom,
            onClick = { onSetCustom(true) }
        )
    }
}

/**
 * Preview swatch showing selected pigment name + brand with color dot.
 */
@Composable
internal fun PigmentPreviewSwatch(
    name: String,
    brand: String,
    colorHex: String
) {
    val spacing = DasurvTheme.spacing
    if (name.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ColorSwatch(colorHex = colorHex, label = "")
            Spacer(modifier = Modifier.width(spacing.md))
            Column {
                Text(name, style = MaterialTheme.typography.titleSmall, color = M3OnSurface)
                Text(brand, style = MaterialTheme.typography.bodySmall, color = M3OnSurfaceVariant)
            }
        }
    }
}

/**
 * Catalog pigment dropdown picker with color swatches in dropdown items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PigmentCatalogDropdown(
    selectedName: String,
    selectedBrand: String,
    allPigments: List<Pigment>,
    onPigmentSelected: (Pigment) -> Unit
) {
    val spacing = DasurvTheme.spacing
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Pigment", style = M3LabelStyle)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(M3FieldBg)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedName.isNotBlank()) "$selectedName ($selectedBrand)" else "Select...",
                    fontSize = 16.sp,
                    color = if (selectedName.isBlank()) M3OnSurfaceVariant.copy(alpha = 0.5f) else M3OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                scrollState = rememberScrollState(),
                shadowElevation = 2.dp
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
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
