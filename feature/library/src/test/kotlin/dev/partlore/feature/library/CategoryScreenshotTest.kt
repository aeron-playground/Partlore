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
import dev.partlore.core.model.ContentResult
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
class CategoryScreenshotTest {
    private val all = ContentResult.Ok(CategoryState(SampleContent.boards))
    private val filtered =
        ContentResult.Ok(CategoryState(SampleContent.boards, CategoryFilter("dev-boards", setOf("wifi"))))

    @Test fun categoryLight() = shot("category_light", Light) { Page(all) }

    @Test fun categoryDark() = shot("category_dark", Dark) { Page(all) }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun categoryLightFont200() = shot("category_light_font200", Light, fontScale = 2f) { Page(all) }

    @Test
    @Config(qualifiers = TABLET)
    fun categoryTabletLight() = shot("category_tablet_light", Light) { Page(all) }

    @Test fun categoryFilteredLight() = shot("category_filtered_light", Light) { Page(filtered) }

    @Composable
    private fun Page(state: ContentResult<CategoryState>) = CategoryScreen(state, {}, {}, {})

    private fun shot(name: String, mode: PartloreThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode, content = content)
            }
        }
}
