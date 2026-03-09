package com.dasurv.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PhotoShareHelper {

    /**
     * Shares a single photo with optional watermark text.
     */
    fun sharePhoto(context: Context, photoUri: String, watermark: String? = null) {
        val bitmap = loadBitmap(photoUri) ?: return
        try {
            val finalBitmap = if (watermark != null) applyWatermark(bitmap, watermark) else bitmap
            try {
                val uri = saveToCacheAndGetUri(context, finalBitmap, "shared_photo.jpg")
                launchShare(context, uri, "Share Photo")
            } finally {
                if (finalBitmap !== bitmap) finalBitmap.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Creates a side-by-side before/after composite and shares it.
     */
    fun shareBeforeAfter(context: Context, beforeUri: String, afterUri: String, watermark: String? = null) {
        val before = loadBitmap(beforeUri) ?: return
        val after = loadBitmap(afterUri) ?: run { before.recycle(); return }

        try {
            val width = before.width + after.width
            val height = maxOf(before.height, after.height)
            val composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            try {
                val canvas = Canvas(composite)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(before, 0f, 0f, null)
                canvas.drawBitmap(after, before.width.toFloat(), 0f, null)

                val finalBitmap = if (watermark != null) applyWatermark(composite, watermark) else composite
                try {
                    val uri = saveToCacheAndGetUri(context, finalBitmap, "before_after.jpg")
                    launchShare(context, uri, "Share Before & After")
                } finally {
                    if (finalBitmap !== composite) finalBitmap.recycle()
                }
            } finally {
                composite.recycle()
            }
        } finally {
            before.recycle()
            after.recycle()
        }
    }

    private fun loadBitmap(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (_: Exception) {
            null
        }
    }

    private fun applyWatermark(bitmap: Bitmap, text: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = result.width * 0.03f
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        val x = result.width * 0.02f
        val y = result.height - result.height * 0.02f
        canvas.drawText(text, x, y, paint)
        return result
    }

    private fun saveToCacheAndGetUri(context: Context, bitmap: Bitmap, filename: String): Uri {
        val dir = File(context.cacheDir, "shared_photos")
        dir.mkdirs()
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun launchShare(context: Context, uri: Uri, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
