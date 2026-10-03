package dev.partlore.feature.onboarding

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Bench
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Dark
import dev.partlore.core.designsystem.theme.PartloreThemeMode.Light
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
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
class OnboardingScreenshotTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    @Test fun welcomeLight() = shot("onboarding_welcome_light", Light)

    @Test fun welcomeDark() = shot("onboarding_welcome_dark", Dark)

    @Test fun welcomeBench() = shot("onboarding_welcome_bench", Bench)

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun welcomeLightFont200() = shot("onboarding_welcome_light_font200", Light, fontScale = 2f)

    @Test
    fun interestsLight() = shot("onboarding_interests_light", Light, initialPage = 3) {
        compose.onNodeWithText("ESP32").performClick()
    }

    private fun shot(
        name: String,
        mode: PartloreThemeMode,
        fontScale: Float = 1f,
        initialPage: Int = 0,
        before: () -> Unit = {},
    ) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                PartloreTheme(mode = mode) { OnboardingScreen(onFinish = {}, initialPage = initialPage) }
            }
        }
        before()
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
