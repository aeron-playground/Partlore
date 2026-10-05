package dev.partlore.feature.part

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import dev.partlore.core.model.Cite
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.PartPage
import dev.partlore.core.testing.SampleContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w411dp-h891dp-xhdpi"

// Tall enough for the whole sample page; the 200 % one needs about twice the height.
private const val PHONE_PAGE = "w411dp-h3200dp-xhdpi"
private const val PHONE_XTALL = "w411dp-h4800dp-xhdpi"
private const val TABLET = "w840dp-h1200dp-mdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class PartScreenshotTest {
    private val ready = ContentResult.Ok(SampleContent.devBoard)
    private val datasheetPage = Cite("s1", 28, null, null)

    @Test
    @Config(qualifiers = PHONE_PAGE)
    fun partLight() = shot("part_light", Light) { Page(ready) }

    @Test
    @Config(qualifiers = PHONE_PAGE)
    fun partDark() = shot("part_dark", Dark) { Page(ready) }

    @Test
    @Config(qualifiers = PHONE_XTALL)
    fun partLightFont200() = shot("part_light_font200", Light, fontScale = 2f) { Page(ready) }

    @Test
    @Config(qualifiers = TABLET)
    fun partTabletLight() = shot("part_tablet_light", Light) { Page(ready) }

    @Test fun partNotFoundLight() = shot("part_not_found_light", Light) { Page(ContentResult.NotFound) }

    @Test fun statusSheetLight() = shot("part_status_sheet_light", Light) {
        Sheet { StatusSheetContent(SampleContent.devBoard) }
    }

    @Test fun statusSheetDark() = shot("part_status_sheet_dark", Dark) {
        Sheet { StatusSheetContent(SampleContent.devBoard) }
    }

    @Test fun sourceSheetLight() = shot("part_source_sheet_light", Light) {
        Sheet { SourceSheetContent(datasheetPage, SampleContent.devBoard.sources) }
    }

    @Test fun sourceSheetDark() = shot("part_source_sheet_dark", Dark) {
        Sheet { SourceSheetContent(datasheetPage, SampleContent.devBoard.sources) }
    }

    @Composable
    private fun Page(state: ContentResult<PartPage>) = PartScreen(state, onBack = {}, onOpenPart = {})

    // A sheet opens in its own window, which a screenshot can't capture; draw its content on the sheet colour.
    @Composable
    private fun Sheet(content: @Composable () -> Unit) {
        Box(
            Modifier.fillMaxWidth().background(
                PartloreTheme.colors.surfaceRaised,
            ).padding(PartloreTheme.spacing.space16),
        ) {
            content()
        }
    }

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }
}
