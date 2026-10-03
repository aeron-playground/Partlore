package dev.partlore.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.partlore.core.designsystem.R

// One static file per weight: variable fonts' weight axis is not applied on real devices.
private val Bricolage =
    FontFamily(
        Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
        Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
    )

private val Atkinson =
    FontFamily(
        Font(R.font.atkinson_hyperlegible_next_regular, FontWeight.Normal),
        Font(R.font.atkinson_hyperlegible_next_medium, FontWeight.Medium),
        Font(R.font.atkinson_hyperlegible_next_semibold, FontWeight.SemiBold),
    )

private val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono_medium, FontWeight.Medium))

// Slashed zero, so 0 and O never look alike in pin names and values.
private const val MONO_FEATURES = "zero"

@Immutable
data class PartloreTypography(
    val displayL: TextStyle =
        TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp),
    val displayM: TextStyle =
        TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    val headline: TextStyle =
        TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    val title: TextStyle =
        TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    val bodyL: TextStyle =
        TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
    val bodyM: TextStyle =
        TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    val label: TextStyle =
        TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    val caption: TextStyle =
        TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    val monoM: TextStyle =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontFeatureSettings = MONO_FEATURES,
        ),
    val monoS: TextStyle =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFeatureSettings = MONO_FEATURES,
        ),
)

internal val DefaultTypography = PartloreTypography()

/** Style names and values, in display order. Used by the Design Catalog. */
fun PartloreTypography.namedStyles(): List<Pair<String, TextStyle>> = listOf(
    "displayL" to displayL, "displayM" to displayM, "headline" to headline, "title" to title,
    "bodyL" to bodyL, "bodyM" to bodyM, "label" to label, "caption" to caption,
    "monoM" to monoM, "monoS" to monoS,
)
