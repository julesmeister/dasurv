package com.dasurv.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val FieldBg = Color(0xFFF0F1FA)
private val DialogBg = Color(0xFFFCFCFF)
private val ButtonBarBg = Color(0xFFF6F6FE)

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
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = wrappedOnValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .then(if (minLines > 1) Modifier.heightIn(min = (minLines * 24 + 28).dp) else Modifier),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = M3OnSurface,
            ),
            singleLine = singleLine,
            keyboardOptions = mergedKeyboardOptions,
            keyboardActions = keyboardActions,
            readOnly = readOnly,
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = M3OnSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
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
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = M3OnSurface,
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
fun DasurvFormDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Save",
    dismissLabel: String = "Cancel",
    onDelete: (() -> Unit)? = null,
    deleteLabel: String = "Delete",
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    confirmEnabled: Boolean = true,
    headerExtra: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DialogBg,
            tonalElevation = 6.dp,
        ) {
            Box {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(M3PrimaryContainer.copy(alpha = 0.35f))
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 20.dp),
                        horizontalAlignment = if (icon != null) Alignment.CenterHorizontally else Alignment.Start,
                    ) {
                        if (icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(icon, null, tint = M3Primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface,
                        )
                        if (headerExtra != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            headerExtra()
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        content()
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ButtonBarBg)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (onDelete != null) {
                            TextButton(onClick = onDelete) {
                                Text(deleteLabel, color = M3RedColor, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FieldBg,
                                contentColor = M3OnSurfaceVariant,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text(dismissLabel, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onConfirm,
                            enabled = confirmEnabled && !isLoading,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = M3Primary,
                                contentColor = Color.White,
                                disabledContainerColor = M3Primary.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f),
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(confirmLabel, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(DialogBg.copy(alpha = 0.6f)),
                    )
                }
            }
        }
    }
}

@Composable
fun DasurvSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = M3Primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FieldBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurface,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor),
        )
    }
}

@Composable
fun DasurvDropdownField(
    value: String,
    label: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FieldBg)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = 16.sp,
                    color = if (value.isEmpty()) M3OnSurfaceVariant.copy(alpha = 0.5f) else M3OnSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = M3OnSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun DasurvSelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = M3Primary,
    avatar: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor else accentColor.copy(alpha = 0.08f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.25f)
                        else accentColor.copy(alpha = 0.10f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                avatar()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else M3OnSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DasurvQuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = Int.MAX_VALUE,
    accentColor: Color = M3Primary,
    valueDisplay: @Composable ((Int) -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.size(36.dp),
            enabled = value > minValue,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp), tint = accentColor)
            }
        }
        if (valueDisplay != null) {
            valueDisplay(value)
        } else {
            Text(
                "$value",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = M3OnSurface,
            )
        }
        Surface(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.size(36.dp),
            enabled = value < maxValue,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = accentColor)
            }
        }
    }
}
