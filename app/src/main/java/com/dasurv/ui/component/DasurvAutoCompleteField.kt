package com.dasurv.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DasurvAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) M3Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    val filtered = remember(value, suggestions) {
        if (value.isBlank()) emptyList()
        else suggestions.filter {
            it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true)
        }
    }
    var showSuggestions by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = M3LabelStyle)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = {
                val capitalized = if (it.isNotEmpty()) it.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.uppercase().first() else c
                } else it
                onValueChange(capitalized)
                showSuggestions = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(M3FieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
            textStyle = TextStyle(fontSize = 16.sp, color = M3OnSurfaceVariant),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 16.sp,
                                color = M3OnSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                    if (value.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(M3OnSurfaceVariant.copy(alpha = 0.10f))
                                .clickable { onValueChange("") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = M3OnSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            },
        )

        // Suggestions dropdown
        if (isFocused && showSuggestions && filtered.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                    items(filtered) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(suggestion)
                                    showSuggestions = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3OnSurface,
                        )
                    }
                }
            }
        }
    }
}
