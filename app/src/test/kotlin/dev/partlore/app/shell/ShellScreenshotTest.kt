package dev.partlore.app.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation3.runtime.NavKey
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.app.navigation.Tab
import dev.partlore.app.navigation.TabsState
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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE)
class ShellScreenshotTest {
    @Test fun libraryLight() = shot("shell_library_light", Light) { PartloreShell(settings = {}) }

    @Test fun libraryDark() = shot("shell_library_dark", Dark) { PartloreShell(settings = {}) }

    @Test fun libraryBench() = shot("shell_library_bench", Bench) { PartloreShell(settings = {}) }

    @Test fun libraryLightFont200() = shot("shell_library_light_font200", Light, fontScale = 2f) {
        PartloreShell(settings = {})
    }

    @Test fun benchTabLight() = shot("shell_bench_tab_light", Light) {
        PartloreShell(settings = {}, tabs = startingOn(Tab.Bench))
    }

    @Test
    @Config(qualifiers = TABLET)
    fun libraryTabletLight() = shot("shell_library_tablet_light", Light) { PartloreShell(settings = {}) }

    private fun startingOn(tab: Tab) =
        TabsState(Tab.entries.associateWith { mutableStateListOf<NavKey>(it.root) }, mutableStateOf(tab))

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }
}
