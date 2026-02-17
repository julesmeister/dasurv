package com.dasurv.ui.screen.camera

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dasurv.ui.theme.RosePrimary

@Composable
internal fun CropOverlay(
    bitmapWidth: Int,
    bitmapHeight: Int,
    screenHeightPx: Float,
    screenWidthPx: Float,
    onApply: (Rect) -> Unit,
    onCancel: () -> Unit,
    onRotateLeft: () -> Unit = {},
    onRotateRight: () -> Unit = {}
) {
    var cropLeft by remember { mutableFloatStateOf(screenWidthPx * 0.1f) }
    var cropTop by remember { mutableFloatStateOf(screenHeightPx * 0.1f) }
    var cropRight by remember { mutableFloatStateOf(screenWidthPx * 0.9f) }
    var cropBottom by remember { mutableFloatStateOf(screenHeightPx * 0.9f) }

    var activeHandle by remember { mutableStateOf<CropHandle?>(null) }

    val handleRadius = 20f

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activeHandle = detectCropHandle(
                                offset, cropLeft, cropTop, cropRight, cropBottom, handleRadius * 2
                            )
                        },
                        onDrag = { _, dragAmount ->
                            when (activeHandle) {
                                CropHandle.TOP_LEFT -> {
                                    cropLeft = (cropLeft + dragAmount.x).coerceIn(0f, cropRight - 50f)
                                    cropTop = (cropTop + dragAmount.y).coerceIn(0f, cropBottom - 50f)
                                }
                                CropHandle.TOP_RIGHT -> {
                                    cropRight = (cropRight + dragAmount.x).coerceIn(cropLeft + 50f, screenWidthPx)
                                    cropTop = (cropTop + dragAmount.y).coerceIn(0f, cropBottom - 50f)
                                }
                                CropHandle.BOTTOM_LEFT -> {
                                    cropLeft = (cropLeft + dragAmount.x).coerceIn(0f, cropRight - 50f)
                                    cropBottom = (cropBottom + dragAmount.y).coerceIn(cropTop + 50f, screenHeightPx)
                                }
                                CropHandle.BOTTOM_RIGHT -> {
                                    cropRight = (cropRight + dragAmount.x).coerceIn(cropLeft + 50f, screenWidthPx)
                                    cropBottom = (cropBottom + dragAmount.y).coerceIn(cropTop + 50f, screenHeightPx)
                                }
                                CropHandle.CENTER -> {
                                    val w = cropRight - cropLeft
                                    val h = cropBottom - cropTop
                                    val newLeft = (cropLeft + dragAmount.x).coerceIn(0f, screenWidthPx - w)
                                    val newTop = (cropTop + dragAmount.y).coerceIn(0f, screenHeightPx - h)
                                    cropLeft = newLeft
                                    cropTop = newTop
                                    cropRight = newLeft + w
                                    cropBottom = newTop + h
                                }
                                null -> {}
                            }
                        },
                        onDragEnd = { activeHandle = null }
                    )
                }
        ) {
            val overlayColor = Color.Black.copy(alpha = 0.6f)

            // Top region
            drawRect(overlayColor, Offset.Zero, androidx.compose.ui.geometry.Size(size.width, cropTop))
            // Bottom region
            drawRect(overlayColor, Offset(0f, cropBottom), androidx.compose.ui.geometry.Size(size.width, size.height - cropBottom))
            // Left region
            drawRect(overlayColor, Offset(0f, cropTop), androidx.compose.ui.geometry.Size(cropLeft, cropBottom - cropTop))
            // Right region
            drawRect(overlayColor, Offset(cropRight, cropTop), androidx.compose.ui.geometry.Size(size.width - cropRight, cropBottom - cropTop))

            // Crop border
            drawRect(
                Color.White,
                Offset(cropLeft, cropTop),
                androidx.compose.ui.geometry.Size(cropRight - cropLeft, cropBottom - cropTop),
                style = Stroke(width = 2f)
            )

            // Grid lines (rule of thirds)
            val thirdW = (cropRight - cropLeft) / 3f
            val thirdH = (cropBottom - cropTop) / 3f
            val gridColor = Color.White.copy(alpha = 0.4f)
            for (i in 1..2) {
                drawLine(gridColor, Offset(cropLeft + thirdW * i, cropTop), Offset(cropLeft + thirdW * i, cropBottom), strokeWidth = 1f)
                drawLine(gridColor, Offset(cropLeft, cropTop + thirdH * i), Offset(cropRight, cropTop + thirdH * i), strokeWidth = 1f)
            }

            // Corner handles
            val handleColor = Color.White
            val handleLen = 24f
            val handleStroke = 4f

            // Top-left
            drawLine(handleColor, Offset(cropLeft, cropTop), Offset(cropLeft + handleLen, cropTop), strokeWidth = handleStroke)
            drawLine(handleColor, Offset(cropLeft, cropTop), Offset(cropLeft, cropTop + handleLen), strokeWidth = handleStroke)
            // Top-right
            drawLine(handleColor, Offset(cropRight, cropTop), Offset(cropRight - handleLen, cropTop), strokeWidth = handleStroke)
            drawLine(handleColor, Offset(cropRight, cropTop), Offset(cropRight, cropTop + handleLen), strokeWidth = handleStroke)
            // Bottom-left
            drawLine(handleColor, Offset(cropLeft, cropBottom), Offset(cropLeft + handleLen, cropBottom), strokeWidth = handleStroke)
            drawLine(handleColor, Offset(cropLeft, cropBottom), Offset(cropLeft, cropBottom - handleLen), strokeWidth = handleStroke)
            // Bottom-right
            drawLine(handleColor, Offset(cropRight, cropBottom), Offset(cropRight - handleLen, cropBottom), strokeWidth = handleStroke)
            drawLine(handleColor, Offset(cropRight, cropBottom), Offset(cropRight, cropBottom - handleLen), strokeWidth = handleStroke)
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rotation buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onRotateLeft,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.RotateLeft, contentDescription = "Rotate left")
                }
                FilledIconButton(
                    onClick = onRotateRight,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate right")
                }
            }

            // Apply / Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val scaleX = bitmapWidth.toFloat() / screenWidthPx
                        val scaleY = bitmapHeight.toFloat() / screenHeightPx
                        val rect = Rect(
                            (cropLeft * scaleX).toInt(),
                            (cropTop * scaleY).toInt(),
                            (cropRight * scaleX).toInt(),
                            (cropBottom * scaleY).toInt()
                        )
                        onApply(rect)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Apply")
                }
            }
        }
    }
}

internal enum class CropHandle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
}

internal fun detectCropHandle(
    offset: Offset,
    left: Float, top: Float, right: Float, bottom: Float,
    threshold: Float
): CropHandle? {
    return when {
        offset.x in (left - threshold)..(left + threshold) && offset.y in (top - threshold)..(top + threshold) -> CropHandle.TOP_LEFT
        offset.x in (right - threshold)..(right + threshold) && offset.y in (top - threshold)..(top + threshold) -> CropHandle.TOP_RIGHT
        offset.x in (left - threshold)..(left + threshold) && offset.y in (bottom - threshold)..(bottom + threshold) -> CropHandle.BOTTOM_LEFT
        offset.x in (right - threshold)..(right + threshold) && offset.y in (bottom - threshold)..(bottom + threshold) -> CropHandle.BOTTOM_RIGHT
        offset.x in left..right && offset.y in top..bottom -> CropHandle.CENTER
        else -> null
    }
}
