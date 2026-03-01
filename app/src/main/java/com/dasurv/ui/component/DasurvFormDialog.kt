package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


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
            color = M3DialogSurfaceBg,
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
                            .background(M3ButtonBarBg)
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
                                containerColor = M3FieldBg,
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
                            .background(M3DialogSurfaceBg.copy(alpha = 0.6f)),
                    )
                }
            }
        }
    }
}
