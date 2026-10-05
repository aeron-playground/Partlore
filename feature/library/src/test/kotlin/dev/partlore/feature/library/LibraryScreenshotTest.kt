package dev.partlore.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
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
class LibraryScreenshotTest {
    private val ready = ContentResult.Ok(SampleContent.library)

    @Test fun libraryLight() = shot("library_light", Light) { Home(ready) }

    @Test fun libraryDark() = shot("library_dark", Dark) { Home(ready) }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun libraryLightFont200() = shot("library_light_font200", Light, fontScale = 2f) { Home(ready) }

    @Test
    @Config(qualifiers = TABLET)
    fun libraryTabletLight() = shot("library_tablet_light", Light) { Home(ready) }

    @Test fun libraryEmptyLight() = shot("library_empty_light", Light) {
        Home(ContentResult.Ok(SampleContent.emptyLibrary))
    }

    @Test fun libraryErrorLight() = shot("library_error_light", Light) {
        Home(ContentResult.Failed(ContentProblem.Damaged, "sample"))
    }

    @Composable
    private fun Home(state: ContentResult<LibraryHome>) = LibraryHomeScreen(state, {}, {}, {})

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }
}
