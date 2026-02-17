package com.dasurv.util

import android.graphics.Color
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.data.model.LipColorCategory
import com.dasurv.data.model.Pigment
import com.dasurv.data.repository.PigmentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class ColorMatcher @Inject constructor(
    private val pigmentRepository: PigmentRepository
) {
    data class PigmentRecommendation(
        val pigment: Pigment,
        val reason: String,
        val matchScore: Float // 0-1, higher is better match
    )

    fun getCorrectiveRecommendations(analysis: LipColorAnalysis): List<PigmentRecommendation> {
        val allPigments = pigmentRepository.getAllPigments()
        return when (analysis.category) {
            LipColorCategory.PALE, LipColorCategory.PALE_PINK -> {
                allPigments.filter { it.intensity == "light" || it.intensity == "medium" }
                    .map { pigment ->
                        PigmentRecommendation(
                            pigment = pigment,
                            reason = "Good for building color on pale lips",
                            matchScore = scoreForPaleLips(pigment)
                        )
                    }
            }
            LipColorCategory.PIGMENTED, LipColorCategory.DARK_RED, LipColorCategory.BROWN -> {
                allPigments.map { pigment ->
                    val isColorCorrector = isOrangeModifier(pigment)
                    val isSkinCorrector = pigment.name.contains("Skin", ignoreCase = true) ||
                            pigment.name.contains("Universal", ignoreCase = true)
                    val reason = when {
                        isColorCorrector -> "Orange modifier — neutralizes dark/blue undertones before lip color"
                        isSkinCorrector -> "Skin-tone corrector for evening out dark pigmentation"
                        pigment.undertone == "warm" -> "Warm undertone helps neutralize dark pigmentation"
                        else -> "Complementary shade for ${analysis.category.displayName} lips"
                    }
                    PigmentRecommendation(
                        pigment = pigment,
                        reason = reason,
                        matchScore = scoreForDarkLips(pigment)
                    )
                }
            }
            else -> {
                allPigments.map { pigment ->
                    PigmentRecommendation(
                        pigment = pigment,
                        reason = "Complementary shade for ${analysis.category.displayName} lips",
                        matchScore = scoreByColorDistance(analysis.dominantColorHex, pigment.colorHex)
                    )
                }
            }
        }.sortedByDescending { it.matchScore }.take(10)
    }

    fun getDesiredResultRecommendations(targetColorHex: String): List<PigmentRecommendation> {
        val allPigments = pigmentRepository.getAllPigments()
        return allPigments.map { pigment ->
            PigmentRecommendation(
                pigment = pigment,
                reason = "Close match to desired result",
                matchScore = scoreByColorDistance(targetColorHex, pigment.colorHex)
            )
        }.sortedByDescending { it.matchScore }.take(10)
    }

    private fun scoreForPaleLips(pigment: Pigment): Float {
        return when {
            pigment.intensity == "light" && pigment.undertone == "warm" -> 0.9f
            pigment.intensity == "light" -> 0.8f
            pigment.intensity == "medium" && pigment.undertone == "warm" -> 0.7f
            pigment.intensity == "medium" -> 0.6f
            else -> 0.3f
        }
    }

    private fun isOrangeModifier(pigment: Pigment): Boolean {
        return pigment.name.contains("Orange", ignoreCase = true) ||
                pigment.name.contains("Saffron", ignoreCase = true) ||
                pigment.name.contains("Squash", ignoreCase = true)
    }

    private fun scoreForDarkLips(pigment: Pigment): Float {
        val isSkinCorrector = pigment.name.contains("Skin", ignoreCase = true) ||
                pigment.name.contains("Universal", ignoreCase = true)
        return when {
            isOrangeModifier(pigment) -> 0.95f
            isSkinCorrector -> 0.90f
            pigment.undertone == "warm" && pigment.intensity == "medium" -> 0.80f
            pigment.undertone == "warm" && pigment.intensity == "light" -> 0.75f
            pigment.undertone == "warm" && pigment.intensity == "dark" -> 0.70f
            pigment.undertone == "neutral" -> 0.50f
            pigment.undertone == "cool" && pigment.intensity == "medium" -> 0.35f
            pigment.undertone == "cool" -> 0.25f
            else -> 0.30f
        }
    }

    /**
     * Blends a natural lip color with a pigment color in LAB space to simulate PMU result.
     * @param naturalHex The detected natural lip color hex
     * @param pigmentHex The pigment color hex
     * @param intensity Blend weight toward pigment (0.0 = all natural, 1.0 = all pigment). Default 0.6.
     * @return The blended result hex string
     */
    fun blendPigmentResult(naturalHex: String, pigmentHex: String, intensity: Float = 0.6f): String {
        return try {
            val natColor = Color.parseColor(naturalHex)
            val pigColor = Color.parseColor(pigmentHex)

            val natLab = rgbToLab(Color.red(natColor), Color.green(natColor), Color.blue(natColor))
            val pigLab = rgbToLab(Color.red(pigColor), Color.green(pigColor), Color.blue(pigColor))

            val blendedL = natLab[0] * (1 - intensity) + pigLab[0] * intensity
            val blendedA = natLab[1] * (1 - intensity) + pigLab[1] * intensity
            val blendedB = natLab[2] * (1 - intensity) + pigLab[2] * intensity

            val rgb = labToRgb(blendedL, blendedA, blendedB)
            String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2])
        } catch (e: Exception) {
            pigmentHex
        }
    }

    private fun rgbToLab(r: Int, g: Int, b: Int): FloatArray {
        // sRGB -> linear RGB -> XYZ -> LAB
        var rr = r / 255.0
        var gg = g / 255.0
        var bb = b / 255.0

        rr = if (rr > 0.04045) ((rr + 0.055) / 1.055).pow(2.4) else rr / 12.92
        gg = if (gg > 0.04045) ((gg + 0.055) / 1.055).pow(2.4) else gg / 12.92
        bb = if (bb > 0.04045) ((bb + 0.055) / 1.055).pow(2.4) else bb / 12.92

        val x = (rr * 0.4124564 + gg * 0.3575761 + bb * 0.1804375) / 0.95047
        val y = (rr * 0.2126729 + gg * 0.7151522 + bb * 0.0721750)
        val z = (rr * 0.0193339 + gg * 0.1191920 + bb * 0.9503041) / 1.08883

        fun f(t: Double): Double = if (t > 0.008856) t.pow(1.0 / 3.0) else 7.787 * t + 16.0 / 116.0

        val l = (116.0 * f(y) - 16.0).toFloat()
        val a = (500.0 * (f(x) - f(y))).toFloat()
        val bLab = (200.0 * (f(y) - f(z))).toFloat()
        return floatArrayOf(l, a, bLab)
    }

    private fun labToRgb(l: Float, a: Float, b: Float): IntArray {
        fun fInv(t: Double): Double = if (t > 0.206893) t.pow(3.0) else (t - 16.0 / 116.0) / 7.787

        val y = fInv((l + 16.0) / 116.0)
        val x = fInv(a / 500.0 + (l + 16.0) / 116.0) * 0.95047
        val z = fInv((l + 16.0) / 116.0 - b / 200.0) * 1.08883

        var rr = x * 3.2404542 + y * -1.5371385 + z * -0.4985314
        var gg = x * -0.9692660 + y * 1.8760108 + z * 0.0415560
        var bb = x * 0.0556434 + y * -0.2040259 + z * 1.0572252

        fun gammaCorrect(c: Double): Double =
            if (c > 0.0031308) 1.055 * c.pow(1.0 / 2.4) - 0.055 else 12.92 * c

        rr = gammaCorrect(rr)
        gg = gammaCorrect(gg)
        bb = gammaCorrect(bb)

        return intArrayOf(
            (rr * 255).toInt().coerceIn(0, 255),
            (gg * 255).toInt().coerceIn(0, 255),
            (bb * 255).toInt().coerceIn(0, 255)
        )
    }

    private fun scoreByColorDistance(hex1: String, hex2: String): Float {
        val c1 = Color.parseColor(hex1)
        val c2 = Color.parseColor(hex2)
        val dr = (Color.red(c1) - Color.red(c2)).toFloat()
        val dg = (Color.green(c1) - Color.green(c2)).toFloat()
        val db = (Color.blue(c1) - Color.blue(c2)).toFloat()
        val distance = sqrt(dr * dr + dg * dg + db * db)
        val maxDistance = sqrt(3f * 255f * 255f)
        return 1f - (distance / maxDistance)
    }

    companion object {
        fun blendPigmentResultStatic(naturalHex: String, pigmentHex: String, intensity: Float = 0.6f): String {
            return try {
                val natColor = Color.parseColor(naturalHex)
                val pigColor = Color.parseColor(pigmentHex)

                fun rgbToLabStatic(r: Int, g: Int, b: Int): FloatArray {
                    var rr = r / 255.0
                    var gg = g / 255.0
                    var bb = b / 255.0
                    rr = if (rr > 0.04045) ((rr + 0.055) / 1.055).pow(2.4) else rr / 12.92
                    gg = if (gg > 0.04045) ((gg + 0.055) / 1.055).pow(2.4) else gg / 12.92
                    bb = if (bb > 0.04045) ((bb + 0.055) / 1.055).pow(2.4) else bb / 12.92
                    val x = (rr * 0.4124564 + gg * 0.3575761 + bb * 0.1804375) / 0.95047
                    val y = (rr * 0.2126729 + gg * 0.7151522 + bb * 0.0721750)
                    val z = (rr * 0.0193339 + gg * 0.1191920 + bb * 0.9503041) / 1.08883
                    fun f(t: Double): Double = if (t > 0.008856) t.pow(1.0 / 3.0) else 7.787 * t + 16.0 / 116.0
                    return floatArrayOf(
                        (116.0 * f(y) - 16.0).toFloat(),
                        (500.0 * (f(x) - f(y))).toFloat(),
                        (200.0 * (f(y) - f(z))).toFloat()
                    )
                }

                fun labToRgbStatic(l: Float, a: Float, b: Float): IntArray {
                    fun fInv(t: Double): Double = if (t > 0.206893) t.pow(3.0) else (t - 16.0 / 116.0) / 7.787
                    val y = fInv((l + 16.0) / 116.0)
                    val x = fInv(a / 500.0 + (l + 16.0) / 116.0) * 0.95047
                    val z = fInv((l + 16.0) / 116.0 - b / 200.0) * 1.08883
                    var rr = x * 3.2404542 + y * -1.5371385 + z * -0.4985314
                    var gg = x * -0.9692660 + y * 1.8760108 + z * 0.0415560
                    var bb = x * 0.0556434 + y * -0.2040259 + z * 1.0572252
                    fun gc(c: Double): Double = if (c > 0.0031308) 1.055 * c.pow(1.0 / 2.4) - 0.055 else 12.92 * c
                    rr = gc(rr); gg = gc(gg); bb = gc(bb)
                    return intArrayOf((rr * 255).toInt().coerceIn(0, 255), (gg * 255).toInt().coerceIn(0, 255), (bb * 255).toInt().coerceIn(0, 255))
                }

                val natLab = rgbToLabStatic(Color.red(natColor), Color.green(natColor), Color.blue(natColor))
                val pigLab = rgbToLabStatic(Color.red(pigColor), Color.green(pigColor), Color.blue(pigColor))
                val blL = natLab[0] * (1 - intensity) + pigLab[0] * intensity
                val blA = natLab[1] * (1 - intensity) + pigLab[1] * intensity
                val blB = natLab[2] * (1 - intensity) + pigLab[2] * intensity
                val rgb = labToRgbStatic(blL, blA, blB)
                String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2])
            } catch (e: Exception) {
                pigmentHex
            }
        }
    }
}
