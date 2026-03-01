package com.dasurv.data.repository

import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigmentRepository @Inject constructor() {

    fun getAllPigments(): List<Pigment> = allPigmentsCached

    fun getPigmentsByBrand(brand: PigmentBrand): List<Pigment> = when (brand) {
        PigmentBrand.PERMABLEND_LUXE -> permablendLuxe
        PigmentBrand.PERMABLEND_ORIGINAL -> permablendOriginal
        PigmentBrand.EVENFLO -> evenflo
        PigmentBrand.TRUNM -> trunm
    }

    fun getPigmentByName(name: String): Pigment? = allPigmentsCached.find { it.name == name }

    companion object {
        private val allPigmentsCached: List<Pigment> by lazy {
            permablendLuxe + permablendOriginal + evenflo + trunm
        }

        val permablendLuxe = listOf(
            Pigment("Amelia Rose", PigmentBrand.PERMABLEND_LUXE, "#D4737E", "warm", "medium", "Soft dusty rose",
                retentionRate = 0.57f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Berry", PigmentBrand.PERMABLEND_LUXE, "#8B2252", "cool", "dark", "Deep berry tone",
                retentionRate = 0.52f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Blossom", PigmentBrand.PERMABLEND_LUXE, "#F4A7B9", "warm", "light", "Light pink blossom",
                retentionRate = 0.48f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Boudoir", PigmentBrand.PERMABLEND_LUXE, "#C25B78", "warm", "medium", "Rich romantic pink",
                retentionRate = 0.58f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Cardinal", PigmentBrand.PERMABLEND_LUXE, "#C41E3A", "cool", "dark", "Bold cardinal red",
                retentionRate = 0.53f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Cherry Red", PigmentBrand.PERMABLEND_LUXE, "#DE3163", "warm", "dark", "Vibrant cherry red",
                retentionRate = 0.55f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Cotton Candy", PigmentBrand.PERMABLEND_LUXE, "#FFB7D5", "warm", "light", "Soft cotton candy pink",
                retentionRate = 0.45f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Cranberry", PigmentBrand.PERMABLEND_LUXE, "#9B1B30", "cool", "dark", "Deep cranberry",
                retentionRate = 0.50f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Henna", PigmentBrand.PERMABLEND_LUXE, "#A0522D", "warm", "dark", "Earthy henna brown",
                retentionRate = 0.58f, healingNotes = "Earth tones hold well; minimal color shift"),
            Pigment("Hot Pink", PigmentBrand.PERMABLEND_LUXE, "#FF69B4", "warm", "medium", "Vibrant hot pink",
                retentionRate = 0.52f, healingNotes = "Vibrant shades fade noticeably; heals softer"),
            Pigment("Muted Orange", PigmentBrand.PERMABLEND_LUXE, "#D2691E", "warm", "medium", "Soft muted orange",
                retentionRate = 0.40f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Naval Orange", PigmentBrand.PERMABLEND_LUXE, "#FF8C00", "warm", "medium", "Bold navel orange",
                retentionRate = 0.38f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Orange Peel", PigmentBrand.PERMABLEND_LUXE, "#FF9F00", "warm", "medium", "Bright orange peel",
                retentionRate = 0.37f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Pink Gala", PigmentBrand.PERMABLEND_LUXE, "#E8728A", "warm", "medium", "Festive pink gala",
                retentionRate = 0.55f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Pomegranate", PigmentBrand.PERMABLEND_LUXE, "#C0392B", "warm", "dark", "Rich pomegranate red",
                retentionRate = 0.56f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Red Apple", PigmentBrand.PERMABLEND_LUXE, "#E74C3C", "warm", "dark", "Classic red apple",
                retentionRate = 0.55f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Rose Royale", PigmentBrand.PERMABLEND_LUXE, "#CE5B78", "cool", "medium", "Regal rose pink",
                retentionRate = 0.54f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Rosewood", PigmentBrand.PERMABLEND_LUXE, "#9E4B5E", "cool", "dark", "Deep rosewood",
                retentionRate = 0.53f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Rouge", PigmentBrand.PERMABLEND_LUXE, "#E0115F", "cool", "dark", "Classic rouge",
                retentionRate = 0.52f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Saffron", PigmentBrand.PERMABLEND_LUXE, "#F4C430", "warm", "light", "Warm saffron modifier",
                retentionRate = 0.35f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Victorian Rose", PigmentBrand.PERMABLEND_LUXE, "#D9899E", "warm", "medium", "Elegant Victorian rose",
                retentionRate = 0.58f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Vintage Maroon", PigmentBrand.PERMABLEND_LUXE, "#800020", "cool", "dark", "Classic vintage maroon",
                retentionRate = 0.52f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Vivid Koral", PigmentBrand.PERMABLEND_LUXE, "#FF6F61", "warm", "medium", "Vivid coral shade",
                retentionRate = 0.53f, healingNotes = "Vibrant shades fade noticeably; heals softer")
        )

        val permablendOriginal = listOf(
            Pigment("Bazooka", PigmentBrand.PERMABLEND_ORIGINAL, "#F77FBE", "warm", "light", "Bright bubblegum pink",
                retentionRate = 0.47f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Blushed", PigmentBrand.PERMABLEND_ORIGINAL, "#DE9AAE", "warm", "light", "Soft blushed pink",
                retentionRate = 0.50f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Creme de Pink", PigmentBrand.PERMABLEND_ORIGINAL, "#F1C6D3", "warm", "light", "Creamy pastel pink",
                retentionRate = 0.45f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Date Night", PigmentBrand.PERMABLEND_ORIGINAL, "#B5485D", "warm", "medium", "Romantic date night pink",
                retentionRate = 0.57f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("French Fancy", PigmentBrand.PERMABLEND_ORIGINAL, "#F0A1BF", "warm", "light", "Delicate French pink",
                retentionRate = 0.48f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Ladybug", PigmentBrand.PERMABLEND_ORIGINAL, "#D21F3C", "warm", "dark", "Bold ladybug red",
                retentionRate = 0.55f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Mauve", PigmentBrand.PERMABLEND_ORIGINAL, "#B784A7", "cool", "medium", "Classic mauve",
                retentionRate = 0.54f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Orange Crush", PigmentBrand.PERMABLEND_ORIGINAL, "#F57F20", "warm", "medium", "Vibrant orange modifier",
                retentionRate = 0.38f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Passion Red", PigmentBrand.PERMABLEND_ORIGINAL, "#CC0000", "warm", "dark", "Intense passion red",
                retentionRate = 0.56f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Pillow Talk", PigmentBrand.PERMABLEND_ORIGINAL, "#DEB5C0", "warm", "light", "Soft pillow talk nude-pink",
                retentionRate = 0.48f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Squash", PigmentBrand.PERMABLEND_ORIGINAL, "#E8862A", "warm", "medium", "Warm squash orange modifier",
                retentionRate = 0.40f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Tres Pink", PigmentBrand.PERMABLEND_ORIGINAL, "#F4889C", "warm", "medium", "Triple pink intensity",
                retentionRate = 0.53f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Sweet Melissa", PigmentBrand.PERMABLEND_ORIGINAL, "#E56B8F", "warm", "medium", "Sweet medium pink",
                retentionRate = 0.55f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Royal Red", PigmentBrand.PERMABLEND_ORIGINAL, "#B22222", "cool", "dark", "Deep royal red",
                retentionRate = 0.53f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Lush Pink", PigmentBrand.PERMABLEND_ORIGINAL, "#EC6D8A", "warm", "medium", "Lush vivid pink",
                retentionRate = 0.54f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Raspberry", PigmentBrand.PERMABLEND_ORIGINAL, "#C72C48", "cool", "dark", "Tart raspberry",
                retentionRate = 0.51f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Fiery Fuchsia", PigmentBrand.PERMABLEND_ORIGINAL, "#DA327C", "cool", "dark", "Bright fiery fuchsia",
                retentionRate = 0.50f, healingNotes = "Vibrant shades fade noticeably; heals softer"),
            Pigment("Dusky Crimson", PigmentBrand.PERMABLEND_ORIGINAL, "#990033", "cool", "dark", "Muted dusky crimson",
                retentionRate = 0.52f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Hollywood Punch", PigmentBrand.PERMABLEND_ORIGINAL, "#E83868", "warm", "dark", "Bold Hollywood punch",
                retentionRate = 0.54f, healingNotes = "Warm reds retain well; slight cooling after heal")
        )

        val evenflo = listOf(
            // Lip Set
            Pigment("Bare", PigmentBrand.EVENFLO, "#F0C4A8", "warm", "light", "Light peachy nude",
                retentionRate = 0.45f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Malina", PigmentBrand.EVENFLO, "#E8927A", "warm", "light", "Peachy pink",
                retentionRate = 0.50f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Lulu's Ros\u00e9", PigmentBrand.EVENFLO, "#E0808F", "warm", "medium", "Pink coral",
                retentionRate = 0.55f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Clay", PigmentBrand.EVENFLO, "#BC4A3C", "warm", "medium", "Brick red",
                retentionRate = 0.58f, healingNotes = "Earth tones hold well; minimal color shift"),
            Pigment("Malbec", PigmentBrand.EVENFLO, "#722F37", "cool", "dark", "Rich burgundy",
                retentionRate = 0.53f, healingNotes = "Cool tones may appear more prominent after healing"),
            // Lip Corrector Set
            Pigment("Neutralizer", PigmentBrand.EVENFLO, "#FF8C00", "warm", "medium", "Bright orange corrector for pink/blue undertones",
                retentionRate = 0.35f, healingNotes = "Designed to fade \u2014 neutralizes undertone before main color"),
            Pigment("Illume", PigmentBrand.EVENFLO, "#E8B830", "warm", "light", "Golden yellow corrector for deep purple hues",
                retentionRate = 0.30f, healingNotes = "Designed to fade \u2014 neutralizes undertone before main color"),
            Pigment("Colorizer", PigmentBrand.EVENFLO, "#DC143C", "cool", "dark", "Bright red corrector for pinkish healed results",
                retentionRate = 0.40f, healingNotes = "Designed to fade \u2014 neutralizes undertone before main color")
        )

        val trunm = listOf(
            Pigment("China Dream Red", PigmentBrand.TRUNM, "#CC3333", "warm", "dark", "Classic Chinese red",
                retentionRate = 0.55f, healingNotes = "Warm reds retain well; slight cooling after heal"),
            Pigment("Tender Pink", PigmentBrand.TRUNM, "#F7B2C4", "warm", "light", "Soft tender pink",
                retentionRate = 0.47f, healingNotes = "Heals softer; may need a second pass"),
            Pigment("Sweet Orange", PigmentBrand.TRUNM, "#ED7D31", "warm", "medium", "Sweet orange modifier",
                retentionRate = 0.40f, healingNotes = "Fades significantly \u2014 apply generously as a corrective base"),
            Pigment("Water Cherry", PigmentBrand.TRUNM, "#D4456A", "cool", "medium", "Fresh water cherry pink",
                retentionRate = 0.54f, healingNotes = "Cool tones may appear more prominent after healing"),
            Pigment("Fashion Bobbi", PigmentBrand.TRUNM, "#E87BA4", "warm", "medium", "Fashionable pink",
                retentionRate = 0.53f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Passionate Rose", PigmentBrand.TRUNM, "#C9506B", "warm", "medium", "Passionate rose pink",
                retentionRate = 0.56f, healingNotes = "Good retention; heals slightly cooler"),
            Pigment("Universal Powder", PigmentBrand.TRUNM, "#F5C5B8", "warm", "light", "Universal powder nude",
                retentionRate = 0.35f, healingNotes = "Designed to fade \u2014 neutralizes undertone before main color"),
            Pigment("Skin Colour", PigmentBrand.TRUNM, "#EFCBB8", "neutral", "light", "Skin-tone corrector",
                retentionRate = 0.32f, healingNotes = "Designed to fade \u2014 neutralizes undertone before main color")
        )
    }
}
