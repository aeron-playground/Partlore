package dev.partlore.feature.pinout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Bench
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.Rotation
import dev.partlore.core.testing.SampleContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w411dp-h891dp-xhdpi"
private const val PHONE_TALL = "w411dp-h2400dp-xhdpi"
private const val TABLET = "w840dp-h1200dp-mdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class PinoutScreenshotTest {
    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }

    private fun ready(
        selected: String? = null,
        table: Boolean = false,
        redLight: Boolean = false,
        bench: Boolean = false,
    ) = ContentResult.Ok(
        PinoutState(
            pinout = SampleContent.devBoardPinout,
            board = PinoutLayout.place(SampleContent.devBoardPinout, Rotation.R0),
            labelMode = PinLabelMode.Board,
            labelModes = listOf(PinLabelMode.Chip, PinLabelMode.Module, PinLabelMode.Board),
            filters = SampleContent.devBoardPinout.filters(),
            filter = null,
            selected = SampleContent.devBoardPinout.pins.firstOrNull { it.id == selected },
            table = table,
            keepScreenOn = false,
            redLight = redLight,
            bench = bench,
            haptics = false,
        ),
    )

    @Composable private fun Viewer(state: ContentResult<PinoutState>) = PinoutScreen(state, onBack = {}, onAction = {})

    @Test fun pinoutLight() = shot("pinout_light", Light) { Viewer(ready()) }

    @Test fun pinoutDark() = shot("pinout_dark", Dark) { Viewer(ready()) }

    @Test fun pinoutBench() = shot("pinout_bench", Bench) { Viewer(ready(bench = true)) }

    @Test fun pinoutRedLight() = shot("pinout_red_light", Dark) { Viewer(ready(redLight = true)) }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun pinoutLightFont200() = shot("pinout_light_font200", Light, fontScale = 2f) { Viewer(ready()) }

    @Test
    @Config(qualifiers = TABLET)
    fun pinoutTabletPinOpen() = shot("pinout_tablet_pin_open", Light) { Viewer(ready(selected = "io2")) }

    @Test fun pinoutTable() = shot("pinout_table_light", Light) { Viewer(ready(table = true)) }

    // What the pin sheet shows; the sheet itself opens in its own window, which a screenshot doesn't capture.
    @Test fun pinDetails() = shot("pinout_pin_details_light", Light) {
        PinDetails(
            (ready(selected = "io2") as ContentResult.Ok).value,
            Modifier.background(PartloreTheme.colors.surfaceRaised).padding(PartloreTheme.spacing.space16),
        )
    }

    @Test fun pinoutNone() = shot("pinout_none_light", Light) { Viewer(ContentResult.NotFound) }
}
