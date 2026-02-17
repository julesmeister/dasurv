package com.dasurv.ui.screen.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.dasurv.ui.util.parseHexSafe
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerOverlay(
    bitmap: Bitmap,
    imageOffsetY: Float,
    onColorPicked: (hexColor: String) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var sampledHex by remember { mutableStateOf<String?>(null) }
    var loupeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val loupeRadiusDp = 40.dp
    val loupeOffsetAboveDp = 110.dp
    val sampleRadius = 15 // px radius for zoomed crop (~30px diameter region)
    val avgRadius = 2 // 5x5 average

    fun sampleColor(bx: Int, by: Int): String {
        var totalR = 0L; var totalG = 0L; var totalB = 0L; var count = 0
        for (dx in -avgRadius..avgRadius) {
            for (dy in -avgRadius..avgRadius) {
                val sx = (bx + dx).coerceIn(0, bitmap.width - 1)
                val sy = (by + dy).coerceIn(0, bitmap.height - 1)
                val pixel = bitmap.getPixel(sx, sy)
                totalR += android.graphics.Color.red(pixel)
                totalG += android.graphics.Color.green(pixel)
                totalB += android.graphics.Color.blue(pixel)
                count++
            }
        }
        if (count == 0) count = 1
        val r = (totalR / count).toInt()
        val g = (totalG / count).toInt()
        val b = (totalB / count).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun mapToBitmap(touchX: Float, touchY: Float): Pair<Int, Int> {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val imageScale = maxOf(screenWidth / bw, screenHeight / bh)
        val drawW = bw * imageScale
        val drawH = bh * imageScale
        val offsetX = (screenWidth - drawW) / 2f
        val offsetY = (screenHeight - drawH) / 2f
        val bitmapX = ((touchX - offsetX) / imageScale).toInt().coerceIn(0, bitmap.width - 1)
        val bitmapY = ((touchY - offsetY - imageOffsetY) / imageScale).toInt().coerceIn(0, bitmap.height - 1)
        return bitmapX to bitmapY
    }

    fun updateSample(touchX: Float, touchY: Float) {
        val (bx, by) = mapToBitmap(touchX, touchY)
        sampledHex = sampleColor(bx, by)
        // Create zoomed crop for loupe
        val cropLeft = (bx - sampleRadius).coerceIn(0, bitmap.width - 1)
        val cropTop = (by - sampleRadius).coerceIn(0, bitmap.height - 1)
        val cropRight = (bx + sampleRadius).coerceIn(0, bitmap.width - 1)
        val cropBottom = (by + sampleRadius).coerceIn(0, bitmap.height - 1)
        val cropW = (cropRight - cropLeft).coerceAtLeast(1)
        val cropH = (cropBottom - cropTop).coerceAtLeast(1)
        loupeBitmap = Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropW, cropH)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                screenWidth = coords.size.width.toFloat()
                screenHeight = coords.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchPos = offset
                        updateSample(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        touchPos = change.position
                        updateSample(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        sampledHex?.let { onColorPicked(it) }
                        touchPos = null
                        sampledHex = null
                        loupeBitmap = null
                    }
                )
            }
    ) {
        // Instruction banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
        ) {
            Text(
                "Touch the lip to sample its natural color",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 4.dp, top = 4.dp)
        ) {
            Icon(Icons.Default.Close, "Cancel", tint = Color.White)
        }

        // Magnifying glass loupe
        val currentTouch = touchPos
        val currentHex = sampledHex
        val currentLoupe = loupeBitmap
        if (currentTouch != null && currentHex != null && currentLoupe != null) {
            val loupeRadiusPx = with(density) { loupeRadiusDp.toPx() }
            val loupeOffsetPx = with(density) { loupeOffsetAboveDp.toPx() }
            val loupeDiameter = loupeRadiusPx * 2

            // Position loupe above touch, clamped to screen
            val loupeCenterX = currentTouch.x
                .coerceIn(loupeRadiusPx, screenWidth - loupeRadiusPx)
            val loupeCenterY = (currentTouch.y - loupeOffsetPx)
                .coerceIn(loupeRadiusPx, screenHeight - loupeRadiusPx - with(density) { 30.dp.toPx() })

            val loupeLeft = (loupeCenterX - loupeRadiusPx).toInt()
            val loupeTop = (loupeCenterY - loupeRadiusPx).toInt()

            val borderColor = parseHexSafe(currentHex)

            Box(modifier = Modifier.offset { IntOffset(loupeLeft, loupeTop) }) {
                // Zoomed bitmap crop
                Image(
                    bitmap = currentLoupe.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(loupeRadiusDp * 2)
                        .clip(CircleShape)
                )

                // Crosshair
                Canvas(modifier = Modifier.size(loupeRadiusDp * 2)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val lineLen = size.width * 0.2f
                    val strokeW = 1.5f
                    drawLine(Color.White, Offset(cx - lineLen, cy), Offset(cx + lineLen, cy), strokeW)
                    drawLine(Color.White, Offset(cx, cy - lineLen), Offset(cx, cy + lineLen), strokeW)
                }

                // Border ring
                Canvas(modifier = Modifier.size(loupeRadiusDp * 2)) {
                    drawCircle(
                        color = borderColor,
                        radius = size.width / 2 - 4f,
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
            }

            // Hex label pill below loupe
            val pillTop = (loupeCenterY + loupeRadiusPx + with(density) { 6.dp.toPx() }).toInt()
            val pillLeft = (loupeCenterX - with(density) { 36.dp.toPx() }).toInt()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.offset { IntOffset(pillLeft, pillTop) }
            ) {
                Text(
                    currentHex,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
