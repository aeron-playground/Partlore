package dev.partlore.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.partlore.core.designsystem.testing.registerComponentActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Draws text at each weight a font family declares and checks that heavier weights really put
 * more ink on screen. Declaring a weight is not enough: the font file must also draw it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FontWeightRenderingTest {
    @get:Rule(order = 0)
    val activity = registerComponentActivity()

    @get:Rule(order = 1)
    val compose = createComposeRule()

    @Test
    fun atkinsonWeightsDrawHeavier() {
        val family = checkNotNull(DefaultTypography.bodyM.fontFamily)
        val ink = inkByWeight(family, listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold))
        assertHeavier(ink, FontWeight.Normal, FontWeight.Medium)
        assertHeavier(ink, FontWeight.Medium, FontWeight.SemiBold)
    }

    @Test
    fun bricolageWeightsDrawHeavier() {
        val family = checkNotNull(DefaultTypography.title.fontFamily)
        val ink = inkByWeight(family, listOf(FontWeight.SemiBold, FontWeight.Bold))
        assertHeavier(ink, FontWeight.SemiBold, FontWeight.Bold)
    }

    private fun assertHeavier(ink: Map<FontWeight, Int>, lighter: FontWeight, heavier: FontWeight) {
        val light = ink.getValue(lighter)
        val heavy = ink.getValue(heavier)
        assertTrue(
            "${heavier.weight} should draw at least 5% more ink than ${lighter.weight}: $heavy vs $light px",
            heavy > light * MIN_RATIO,
        )
    }

    private fun inkByWeight(family: FontFamily, weights: List<FontWeight>): Map<FontWeight, Int> {
        compose.setContent {
            Column(Modifier.background(Color.White)) {
                // Keep every sample below the test activity's title bar.
                Spacer(Modifier.height(96.dp))
                weights.forEach { weight ->
                    Text(
                        text = SAMPLE,
                        style = TextStyle(
                            fontFamily = family,
                            fontWeight = weight,
                            fontSize = 32.sp,
                            color = Color.Black,
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("w${weight.weight}"),
                    )
                }
            }
        }
        return weights.associateWith { weight ->
            val pixels = compose.onNodeWithTag("w${weight.weight}").captureToImage().toPixelMap()
            var dark = 0
            for (x in 0 until pixels.width) {
                for (y in 0 until pixels.height) {
                    if (pixels[x, y].luminance() < 0.5f) dark++
                }
            }
            dark
        }
    }

    private companion object {
        const val SAMPLE = "Hamburgefonstiv"
        const val MIN_RATIO = 1.05f
    }
}
