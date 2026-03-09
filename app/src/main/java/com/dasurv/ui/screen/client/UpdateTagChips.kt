package com.dasurv.ui.screen.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.UpdateTags
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3LabelStyle
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3RedColor

// ── Tag chip colors ────────────────────────────────────────────────

internal fun tagColor(tag: String): Color = when (UpdateTags.categoryOf(tag)) {
    "outcome" -> M3Primary
    "healing" -> M3AmberColor
    "action" -> when (tag) {
        "no follow-up needed" -> M3GreenColor
        else -> M3RedColor
    }
    else -> M3CyanColor
}

// ── Tag Chip (read-only display) ───────────────────────────────────

@Composable
fun UpdateTagChip(tag: String) {
    val color = tagColor(tag)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.10f),
    ) {
        Text(
            tag,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

// ── Selectable Tag Chip (for dialog) ───────────────────────────────

@Composable
internal fun SelectableTagChip(
    tag: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = tagColor(tag)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color.copy(alpha = 0.20f) else M3FieldBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                tag,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) color else M3OnSurfaceVariant,
            )
        }
    }
}

// ── Tag Selection Section ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagSelectionSection(
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    customTag: String,
    onCustomTagChange: (String) -> Unit,
    onAddCustomTag: () -> Unit
) {
    Column {
        Text("Outcome", style = M3LabelStyle, color = M3Primary)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UpdateTags.OUTCOME.forEach { tag ->
                SelectableTagChip(tag, tag in selectedTags) { onToggleTag(tag) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Healing", style = M3LabelStyle, color = M3AmberColor)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UpdateTags.HEALING.forEach { tag ->
                SelectableTagChip(tag, tag in selectedTags) { onToggleTag(tag) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Action", style = M3LabelStyle, color = M3RedColor)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UpdateTags.ACTION.forEach { tag ->
                SelectableTagChip(tag, tag in selectedTags) { onToggleTag(tag) }
            }
        }

        // Custom tags already added
        val customSelected = selectedTags.filter { it !in UpdateTags.ALL }
        if (customSelected.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Custom", style = M3LabelStyle, color = M3CyanColor)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                customSelected.forEach { tag ->
                    SelectableTagChip(tag, true) { onToggleTag(tag) }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DasurvTextField(
                value = customTag,
                onValueChange = onCustomTagChange,
                label = "Custom Tag",
                modifier = Modifier.weight(1f),
                placeholder = "e.g. uneven border",
                autoCapitalize = false,
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onAddCustomTag,
                enabled = customTag.isNotBlank(),
                modifier = Modifier.padding(top = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = M3CyanColor.copy(alpha = 0.10f),
                    contentColor = M3CyanColor,
                ),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
