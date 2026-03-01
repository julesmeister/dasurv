package com.dasurv.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.R

val InterFontFamily = FontFamily(Font(R.font.inter_regular))

private val FieldBg = Color(0xFFF0F1FA)

object FormDefaults {
    val LabelStyle: TextStyle
        @Composable get() = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
            fontFamily = InterFontFamily,
        )

    val ValueStyle: TextStyle
        @Composable get() = TextStyle(
            fontSize = 16.sp,
            color = M3OnSurface,
            fontWeight = FontWeight.Medium,
            fontFamily = InterFontFamily,
        )

    val CursorColor = M3Primary
    val LabelWidth = 110.dp
}

@Composable
fun FormRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    labelWidth: Dp = FormDefaults.LabelWidth,
    labelStyle: TextStyle = FormDefaults.LabelStyle,
    textStyle: TextStyle = FormDefaults.ValueStyle,
    cursorColor: Color = FormDefaults.CursorColor
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) M3Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        Text(
            text = label,
            style = labelStyle,
            modifier = Modifier.width(labelWidth)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = textStyle,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(cursorColor),
            interactionSource = interactionSource,
        )
    }
}

@Composable
fun FormClickableRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelWidth: Dp = FormDefaults.LabelWidth,
    labelStyle: TextStyle = FormDefaults.LabelStyle,
    textStyle: TextStyle = FormDefaults.ValueStyle
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            modifier = Modifier.width(labelWidth)
        )
        Text(
            text = value,
            style = textStyle,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownRow(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelWidth: Dp = FormDefaults.LabelWidth,
    labelStyle: TextStyle = FormDefaults.LabelStyle,
    textStyle: TextStyle = FormDefaults.ValueStyle
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldBg)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = labelStyle,
                modifier = Modifier.width(labelWidth)
            )
            Text(
                text = value,
                style = textStyle,
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            scrollState = rememberScrollState(),
            shadowElevation = 0.dp
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = FormDefaults.LabelStyle
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = labelStyle.copy(
                color = M3OnSurface,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = M3Primary),
        )
    }
}
