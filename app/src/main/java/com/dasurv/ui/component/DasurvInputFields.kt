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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun DasurvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    autoCapitalize: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) M3Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    val mergedKeyboardOptions = if (autoCapitalize &&
        keyboardOptions.capitalization == KeyboardCapitalization.None
    ) {
        keyboardOptions.copy(capitalization = KeyboardCapitalization.Sentences)
    } else {
        keyboardOptions
    }

    val wrappedOnValueChange: (String) -> Unit = if (autoCapitalize) {
        { newValue ->
            onValueChange(
                if (newValue.isNotEmpty()) newValue.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.uppercase().first() else c
                } else newValue
            )
        }
    } else {
        onValueChange
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = M3LabelStyle)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = wrappedOnValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(M3FieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .then(if (minLines > 1) Modifier.heightIn(min = (minLines * 24 + 28).dp) else Modifier),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = M3InputText,
            ),
            singleLine = singleLine,
            keyboardOptions = mergedKeyboardOptions,
            keyboardActions = keyboardActions,
            readOnly = readOnly,
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
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
                    if (value.isNotEmpty() && enabled && !readOnly) {
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
    }
}

@Composable
fun DasurvCurrencyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) M3Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = M3LabelStyle)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(M3FieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = M3InputText,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(M3Primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "\u20B1",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3Primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 16.sp,
                                color = M3OnSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                    if (value.isNotEmpty() && enabled) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            },
        )
    }
}

@Composable
fun DasurvSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) M3Primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(M3FieldBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(50)),
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = M3InputText,
        ),
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(M3Primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = M3OnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = M3OnSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
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
}
