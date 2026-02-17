package com.dasurv.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dasurv.R

val InterFontFamily = FontFamily(Font(R.font.inter_regular))

object FormDefaults {
    val LabelStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFB0B0B0),
            fontFamily = InterFontFamily
        )

    val ValueStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF263238),
            fontWeight = FontWeight.Bold,
            fontFamily = InterFontFamily
        )

    val CursorColor = Color(0xFF78909C)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
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
            cursorBrush = SolidColor(cursorColor)
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
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .padding(vertical = 14.dp),
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = labelStyle.copy(
                color = Color(0xFF263238),
                fontWeight = FontWeight.Bold
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
