package com.dasurv.util

import com.dasurv.data.model.LipColorCategory
import com.dasurv.data.model.Pigment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealingGuide @Inject constructor(
    private val colorMatcher: ColorMatcher
) {
    data class HealingPrediction(
        val healedColorHex: String,
        val retentionPercent: Int,
        val colorShiftDescription: String,
        val pigmentNotes: String,
        val categoryAdvice: String,
        val timeline: List<TimelineStep>
    )

    data class TimelineStep(
        val label: String,
        val description: String
    )

    fun predict(
        naturalHex: String,
        pigment: Pigment,
        lipCategory: LipColorCategory,
        intensity: Float
    ): HealingPrediction {
        val healedIntensity = intensity * pigment.retentionRate
        val healedColorHex = computeHealedColor(naturalHex, pigment.colorHex, healedIntensity, pigment)
        val retentionPercent = (pigment.retentionRate * 100).toInt()
        val colorShift = describeColorShift(pigment)
        val advice = getCategoryAdvice(lipCategory)
        val timeline = buildTimeline(retentionPercent)

        return HealingPrediction(
            healedColorHex = healedColorHex,
            retentionPercent = retentionPercent,
            colorShiftDescription = colorShift,
            pigmentNotes = pigment.healingNotes,
            categoryAdvice = advice,
            timeline = timeline
        )
    }

    private fun computeHealedColor(
        naturalHex: String,
        pigmentHex: String,
        healedIntensity: Float,
        pigment: Pigment
    ): String {
        // Blend at reduced intensity to simulate fading
        val baseHealed = colorMatcher.blendPigmentResult(naturalHex, pigmentHex, healedIntensity)

        // Apply warmth shift: warm pigments lose some warmth after healing
        return try {
            val color = android.graphics.Color.parseColor(baseHealed)
            val r = android.graphics.Color.red(color)
            val g = android.graphics.Color.green(color)
            val b = android.graphics.Color.blue(color)
            val lab = rgbToLab(r, g, b)

            // Warm pigments shift slightly cooler (reduce 'a' channel by 5-10%)
            val aShift = when (pigment.undertone) {
                "warm" -> -lab[1] * 0.07f
                "cool" -> lab[1] * 0.03f // cool tones slightly intensify
                else -> 0f
            }
            val adjustedA = lab[1] + aShift

            val rgb = labToRgb(lab[0], adjustedA, lab[2])
            String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2])
        } catch (_: Exception) {
            baseHealed
        }
    }

    private fun describeColorShift(pigment: Pigment): String {
        return when {
            pigment.retentionRate < 0.40f -> "Fades substantially; designed as a corrective layer"
            pigment.undertone == "warm" && pigment.retentionRate < 0.50f -> "Fades noticeably; heals lighter and softer"
            pigment.undertone == "warm" -> "Heals slightly cooler with mild warmth reduction"
            pigment.undertone == "cool" -> "Cool tones may appear more prominent after healing"
            else -> "Minimal color shift expected"
        }
    }

    fun getCategoryAdvice(category: LipColorCategory): String {
        return when (category) {
            LipColorCategory.PALE, LipColorCategory.PALE_PINK ->
                "Pale lips absorb pigment well with predictable results. Light to medium shades work best. Expect 50-60% retention on first pass."
            LipColorCategory.LIGHT_PINK ->
                "Light pink lips offer a clean canvas. Most pigments perform well. Standard application technique recommended."
            LipColorCategory.MEDIUM_PINK ->
                "Medium pink lips have moderate natural pigment. Warm shades blend naturally. Good candidate for single-pass results."
            LipColorCategory.ROSE, LipColorCategory.MAUVE ->
                "Rose/mauve lips have cool undertones that may compete with warm pigments. Consider a light orange modifier base to neutralize."
            LipColorCategory.DARK_PINK ->
                "Dark pink lips require slightly more saturation. Expect the natural lip color to show through more prominently after healing."
            LipColorCategory.RED ->
                "Red-toned lips benefit from warm pigments. Orange modifier as a base layer can help achieve the target shade."
            LipColorCategory.DARK_RED, LipColorCategory.BROWN ->
                "Dark lips typically retain 30-40% of applied pigment. Orange modifier as a base layer is strongly recommended. Plan for 2-3 sessions."
            LipColorCategory.PIGMENTED ->
                "Highly pigmented lips typically retain 30-45% of applied pigment. Orange modifier as a base layer is strongly recommended. Plan for 2-3 sessions."
        }
    }

    private fun buildTimeline(retentionPercent: Int): List<TimelineStep> = listOf(
        TimelineStep(
            "Day 1-3",
            "Swollen and tender. Color appears very saturated and dark \u2014 this is normal. Keep lips moisturized."
        ),
        TimelineStep(
            "Week 1-2",
            "Peeling and flaking begins. Color lightens significantly (up to 50%). Do not pick at the skin."
        ),
        TimelineStep(
            "Week 4-6",
            "True color emerges as skin fully regenerates. Expect ~${retentionPercent}% of original intensity retained."
        ),
        TimelineStep(
            "3+ Months",
            "Stable result established. Touch-up session recommended at 6-8 weeks if additional saturation is desired."
        )
    )

    // LAB conversion helpers (matching ColorMatcher)
    private fun rgbToLab(r: Int, g: Int, b: Int): FloatArray {
        var rr = r / 255.0
        var gg = g / 255.0
        var bb = b / 255.0
        rr = if (rr > 0.04045) Math.pow((rr + 0.055) / 1.055, 2.4) else rr / 12.92
        gg = if (gg > 0.04045) Math.pow((gg + 0.055) / 1.055, 2.4) else gg / 12.92
        bb = if (bb > 0.04045) Math.pow((bb + 0.055) / 1.055, 2.4) else bb / 12.92
        val x = (rr * 0.4124564 + gg * 0.3575761 + bb * 0.1804375) / 0.95047
        val y = (rr * 0.2126729 + gg * 0.7151522 + bb * 0.0721750)
        val z = (rr * 0.0193339 + gg * 0.1191920 + bb * 0.9503041) / 1.08883
        fun f(t: Double): Double = if (t > 0.008856) Math.pow(t, 1.0 / 3.0) else 7.787 * t + 16.0 / 116.0
        return floatArrayOf(
            (116.0 * f(y) - 16.0).toFloat(),
            (500.0 * (f(x) - f(y))).toFloat(),
            (200.0 * (f(y) - f(z))).toFloat()
        )
    }

    private fun labToRgb(l: Float, a: Float, b: Float): IntArray {
        fun fInv(t: Double): Double = if (t > 0.206893) Math.pow(t, 3.0) else (t - 16.0 / 116.0) / 7.787
        val y = fInv((l + 16.0) / 116.0)
        val x = fInv(a / 500.0 + (l + 16.0) / 116.0) * 0.95047
        val z = fInv((l + 16.0) / 116.0 - b / 200.0) * 1.08883
        var rr = x * 3.2404542 + y * -1.5371385 + z * -0.4985314
        var gg = x * -0.9692660 + y * 1.8760108 + z * 0.0415560
        var bb = x * 0.0556434 + y * -0.2040259 + z * 1.0572252
        fun gc(c: Double): Double = if (c > 0.0031308) 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055 else 12.92 * c
        rr = gc(rr); gg = gc(gg); bb = gc(bb)
        return intArrayOf(
            (rr * 255).toInt().coerceIn(0, 255),
            (gg * 255).toInt().coerceIn(0, 255),
            (bb * 255).toInt().coerceIn(0, 255)
        )
    }
}
