package com.dasurv.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.window.DialogProperties

private val DialogBg = Color(0xFFFCFCFF)
private val ButtonBarBg = Color(0xFFF6F6FE)
private val FieldBg = Color(0xFFF0F1FA)

@Composable
fun DasurvMultiPageDialog(
    title: (page: Int) -> String,
    icon: (page: Int) -> ImageVector,
    accentColor: Color = M3Primary,
    pageCount: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isEdit: Boolean = false,
    confirmEnabled: Boolean = true,
    confirmLabel: (page: Int) -> String = { page ->
        if (page < pageCount - 1) "Next" else if (isEdit) "Update" else "Save"
    },
    isSaving: Boolean = false,
    onConfirm: () -> Unit,
    headerExtra: @Composable (() -> Unit)? = null,
    showPageIndicators: Boolean = !isEdit,
    pageContent: @Composable (page: Int) -> Unit,
) {
    val headerBg = if (accentColor == M3Primary) M3PrimaryContainer.copy(alpha = 0.35f)
                   else accentColor.copy(alpha = 0.08f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DialogBg,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon(currentPage), null, tint = accentColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        title(currentPage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = M3OnSurface,
                    )
                    if (headerExtra != null) {
                        headerExtra()
                    }
                    if (showPageIndicators && pageCount > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(pageCount) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(if (i == currentPage) 20.dp else 8.dp, 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (i == currentPage) accentColor else accentColor.copy(alpha = 0.25f)),
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = currentPage,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "multiPageDialog",
                ) { page ->
                    pageContent(page)
                }

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
                            Text("Delete", color = M3RedColor, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (currentPage > 0) {
                        Button(
                            onClick = { onPageChange(currentPage - 1) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FieldBg, contentColor = M3OnSurfaceVariant,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text("Back", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    if (currentPage == 0) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FieldBg, contentColor = M3OnSurfaceVariant,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = {
                            if (currentPage < pageCount - 1) {
                                onPageChange(currentPage + 1)
                            } else {
                                onConfirm()
                            }
                        },
                        enabled = confirmEnabled && !isSaving,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor, contentColor = Color.White,
                            disabledContainerColor = accentColor.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        if (isSaving && currentPage == pageCount - 1) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(
                                confirmLabel(currentPage),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                            if (currentPage < pageCount - 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
