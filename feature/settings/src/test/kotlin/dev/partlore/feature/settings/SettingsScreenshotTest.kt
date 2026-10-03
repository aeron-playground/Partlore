package dev.partlore.feature.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Bench
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w411dp-h891dp-xhdpi"

// Tall enough to show the whole page at 200% text.
private const val PHONE_TALL = "w411dp-h2400dp-xhdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class SettingsScreenshotTest {
    @Test fun settingsLight() = shot("settings_light", Light)

    @Test fun settingsDark() = shot("settings_dark", Dark)

    @Test fun settingsBench() = shot("settings_bench", Bench)

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun settingsLightFont200() = shot("settings_light_font200", Light, fontScale = 2f)

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode) {
                    SettingsScreen(
                        settings = UserSettings(theme = ThemeSetting.Dark, haptics = true, sounds = false),
                        versionName = "0.0.2",
                        onBack = {},
                        onChange = {},
                    )
                }
            }
        }
}
