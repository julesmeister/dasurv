package com.dasurv.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DasurvConfirmDialog(
    onDismissRequest: () -> Unit,
    icon: ImageVector,
    iconTint: Color = M3RedColor,
    title: String,
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    confirmColor: Color = M3RedColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onDismissRequest
) {
    DasurvConfirmDialog(
        onDismissRequest = onDismissRequest,
        icon = icon,
        iconTint = iconTint,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = M3OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmText = confirmText,
        dismissText = dismissText,
        confirmColor = confirmColor,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DasurvConfirmDialog(
    onDismissRequest: () -> Unit,
    icon: ImageVector,
    iconTint: Color = M3RedColor,
    title: String,
    content: @Composable () -> Unit,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    confirmColor: Color = M3RedColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onDismissRequest
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(24.dp),
            color = M3DialogSurfaceBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = M3OnSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                content()

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3FieldBg,
                            contentColor = M3OnSurfaceVariant,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(dismissText, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(confirmText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
