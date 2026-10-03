package dev.partlore.core.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Bench
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w411dp-h891dp-xhdpi"
private const val TABLET = "w840dp-h1200dp-mdpi"

// 200% text makes sections taller than a phone screen; a tall screen captures all of it.
private const val PHONE_TALL = "w411dp-h2400dp-xhdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class DesignCatalogScreenshotTest {
    @Test fun colorsLight() = shot("colors_light", Light) { ColorCatalog() }

    @Test fun colorsDark() = shot("colors_dark", Dark) { ColorCatalog() }

    @Test fun colorsBench() = shot("colors_bench", Bench) { ColorCatalog() }

    @Test fun typeLight() = shot("type_light", Light) { TypographyCatalog() }

    @Test fun typeDark() = shot("type_dark", Dark) { TypographyCatalog() }

    @Test fun typeBench() = shot("type_bench", Bench) { TypographyCatalog() }

    @Test fun spacingLight() = shot("spacing_light", Light) { SpacingShapeCatalog() }

    @Test fun spacingDark() = shot("spacing_dark", Dark) { SpacingShapeCatalog() }

    @Test fun spacingBench() = shot("spacing_bench", Bench) { SpacingShapeCatalog() }

    @Test fun elevationLight() = shot("elevation_light", Light) { ElevationCatalog() }

    @Test fun elevationDark() = shot("elevation_dark", Dark) { ElevationCatalog() }

    @Test fun elevationBench() = shot("elevation_bench", Bench) { ElevationCatalog() }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun colorsLightFont200() = shot("colors_light_font200", Light, fontScale = 2f) { ColorCatalog() }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun typeLightFont200() = shot("type_light_font200", Light, fontScale = 2f) { TypographyCatalog() }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun spacingLightFont200() = shot("spacing_light_font200", Light, fontScale = 2f) { SpacingShapeCatalog() }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun elevationLightFont200() = shot("elevation_light_font200", Light, fontScale = 2f) { ElevationCatalog() }

    @Test
    @Config(qualifiers = TABLET)
    fun colorsLightTablet() = shot("colors_light_tablet", Light) { ColorCatalog() }

    @Test
    @Config(qualifiers = TABLET)
    fun typeLightTablet() = shot("type_light_tablet", Light) { TypographyCatalog() }

    @Test fun componentsLight() = shot("components_light", Light) { ComponentsCatalog() }

    @Test fun componentsDark() = shot("components_dark", Dark) { ComponentsCatalog() }

    @Test fun componentsBench() = shot("components_bench", Bench) { ComponentsCatalog() }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun componentsLightFont200() = shot("components_light_font200", Light, fontScale = 2f) { ComponentsCatalog() }

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }
}
