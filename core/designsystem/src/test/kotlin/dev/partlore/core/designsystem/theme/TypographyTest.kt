package dev.partlore.core.designsystem.theme

import androidx.compose.ui.text.font.FontListFontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTest {
    @Test
    fun everyStyleUsesAWeightItsFontFamilyDeclares() {
        for ((name, style) in DefaultTypography.namedStyles()) {
            val family = style.fontFamily as FontListFontFamily
            val weights = family.fonts.map { it.weight }
            assertTrue("$name uses ${style.fontWeight}, family has $weights", style.fontWeight in weights)
        }
    }

    @Test
    fun allTenSpecStylesExist() {
        val expected =
            listOf("displayL", "displayM", "headline", "title", "bodyL", "bodyM", "label", "caption", "monoM", "monoS")
        assertEquals(expected, DefaultTypography.namedStyles().map { it.first })
    }
}
