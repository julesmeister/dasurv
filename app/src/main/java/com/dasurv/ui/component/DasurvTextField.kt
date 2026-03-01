package com.dasurv.ui.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FieldBgColor = Color(0xFFF0F1FA)

@Composable
fun DasurvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    colors: TextFieldColors? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = colors ?: TextFieldDefaults.colors(
            focusedContainerColor = FieldBgColor,
            unfocusedContainerColor = FieldBgColor,
            disabledContainerColor = FieldBgColor,
            focusedIndicatorColor = M3Primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = M3RedColor,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DasurvDropdownTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        readOnly = readOnly,
        enabled = enabled,
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = colors ?: TextFieldDefaults.colors(
            focusedContainerColor = FieldBgColor,
            unfocusedContainerColor = FieldBgColor,
            disabledContainerColor = FieldBgColor,
            focusedIndicatorColor = M3Primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = M3RedColor,
        )
    )
}
