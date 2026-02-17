package com.dasurv.ui.screen.camera

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dasurv.ui.theme.RoseTertiary
import com.dasurv.util.ColorMatcher
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour

@Composable
fun LipOverlay(
    face: Face?,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier,
    arMode: Boolean = false,
    upperLipPigmentHex: String? = null,
    lowerLipPigmentHex: String? = null,
    naturalUpperLipHex: String? = null,
    naturalLowerLipHex: String? = null,
    fullCoverage: Boolean = true,
    verticalOffsetPx: Float = 0f,
    upperHorizontalScale: Float = 1.0f,
    lowerHorizontalScale: Float = 1.0f
) {
    if (face == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        // Compute lip centroid for horizontal scaling
        val allLipPoints = listOfNotNull(
            face.getContour(FaceContour.UPPER_LIP_TOP)?.points,
            face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points,
            face.getContour(FaceContour.LOWER_LIP_TOP)?.points,
            face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points
        ).flatten()
        val lipCenterX = if (allLipPoints.isNotEmpty()) {
            val cx = allLipPoints.map { it.x }.average().toFloat()
            if (isFrontCamera) size.width - cx * scaleX else cx * scaleX
        } else 0f

        fun baseX(x: Float): Float = if (isFrontCamera) size.width - x * scaleX else x * scaleX
        fun scaledX(x: Float, hScale: Float): Float {
            val bx = baseX(x)
            return if (hScale != 1.0f) lipCenterX + (bx - lipCenterX) * hScale else bx
        }
        fun translateXUpper(x: Float): Float = scaledX(x, upperHorizontalScale)
        fun translateXLower(x: Float): Float = scaledX(x, lowerHorizontalScale)
        fun translateY(y: Float): Float = y * scaleY + verticalOffsetPx

        val upperTop = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
        val upperBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
        val lowerTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points
        val lowerBottom = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points

        // Draw upper lip — only when a pigment is selected for it
        if (upperTop != null && upperBottom != null && upperLipPigmentHex != null) {
            val upperPath = buildSmoothLipPath(upperTop, upperBottom, ::translateXUpper, ::translateY)
            val fillHex = if (naturalUpperLipHex != null) {
                ColorMatcher.blendPigmentResultStatic(naturalUpperLipHex, upperLipPigmentHex)
            } else upperLipPigmentHex
            drawPath(upperPath, parseHexColor(fillHex).copy(alpha = 0.50f), style = Fill, blendMode = BlendMode.SrcOver)
            drawPath(upperPath, RoseTertiary, style = Stroke(width = 2.5f))
        }

        // Draw lower lip — only when a pigment is selected for it
        if (lowerTop != null && lowerBottom != null && lowerLipPigmentHex != null) {
            val lowerPath = buildSmoothLipPath(lowerTop, lowerBottom, ::translateXLower, ::translateY)
            val fillHex = if (naturalLowerLipHex != null) {
                ColorMatcher.blendPigmentResultStatic(naturalLowerLipHex, lowerLipPigmentHex)
            } else lowerLipPigmentHex
            drawPath(lowerPath, parseHexColor(fillHex).copy(alpha = 0.50f), style = Fill, blendMode = BlendMode.SrcOver)
            drawPath(lowerPath, RoseTertiary, style = Stroke(width = 2.5f))
        }

        // Face bounding box
        val bounds = face.boundingBox
        val faceLeft = baseX(bounds.right.toFloat())
        val faceRight = baseX(bounds.left.toFloat())
        drawRect(
            color = RoseTertiary.copy(alpha = 0.25f),
            topLeft = androidx.compose.ui.geometry.Offset(
                minOf(faceLeft, faceRight),
                translateY(bounds.top.toFloat())
            ),
            size = androidx.compose.ui.geometry.Size(
                Math.abs(faceRight - faceLeft),
                bounds.height().toFloat() * scaleY
            ),
            style = Stroke(width = 1.5f)
        )
    }
}

/**
 * Builds a smooth lip path using cubic Bézier curves between consecutive contour points.
 * Traces topPoints left→right, then bottomPoints right→left, closing the shape.
 */
private fun DrawScope.buildSmoothLipPath(
    topPoints: List<PointF>,
    bottomPoints: List<PointF>,
    translateX: (Float) -> Float,
    translateY: (Float) -> Float
): Path {
    val path = Path()
    if (topPoints.isEmpty() || bottomPoints.isEmpty()) return path

    fun tx(p: PointF) = translateX(p.x)
    fun ty(p: PointF) = translateY(p.y)

    // Start at first top point
    path.moveTo(tx(topPoints.first()), ty(topPoints.first()))

    // Trace top contour with cubic Bézier curves
    addSmoothCurves(path, topPoints, ::tx, ::ty)

    // Trace bottom contour reversed with cubic Bézier curves
    val reversedBottom = bottomPoints.reversed()
    // Line to first reversed-bottom point to bridge the gap
    path.lineTo(tx(reversedBottom.first()), ty(reversedBottom.first()))
    addSmoothCurves(path, reversedBottom, ::tx, ::ty)

    path.close()
    return path
}

/**
 * Adds smooth cubic Bézier curve segments through a list of points.
 * Uses averaged control points for natural curves.
 */
private fun addSmoothCurves(
    path: Path,
    points: List<PointF>,
    tx: (PointF) -> Float,
    ty: (PointF) -> Float
) {
    if (points.size < 2) return

    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]

        if (points.size == 2) {
            path.lineTo(tx(p1), ty(p1))
            return
        }

        // Control points: use neighboring points to compute tangent direction
        val prev = if (i > 0) points[i - 1] else p0
        val next = if (i + 2 < points.size) points[i + 2] else p1

        val cp1x = tx(p0) + (tx(p1) - tx(prev)) / 6f
        val cp1y = ty(p0) + (ty(p1) - ty(prev)) / 6f
        val cp2x = tx(p1) - (tx(next) - tx(p0)) / 6f
        val cp2y = ty(p1) - (ty(next) - ty(p0)) / 6f

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, tx(p1), ty(p1))
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFFFF69B4)
    }
}
