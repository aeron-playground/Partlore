package dev.partlore.feature.onboarding

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import dev.partlore.core.designsystem.theme.MotionPreference
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.BuildInterest
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Native graphics measure real text; the default mode counts every letter as one pixel wide.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    @Test
    fun skipFinishesWithNoAnswers() {
        val finished = mutableListOf<Set<BuildInterest>>()
        compose.setContent { PartloreTheme { OnboardingScreen(onFinish = { finished += it }) } }
        compose.onNodeWithText("Skip").performClick()
        assertEquals(listOf(emptySet<BuildInterest>()), finished)
    }

    @Test
    fun skipKeepsTheInterestsAlreadyPicked() {
        val finished = mutableListOf<Set<BuildInterest>>()
        compose.setContent { PartloreTheme { OnboardingScreen(onFinish = { finished += it }, initialPage = 3) } }
        compose.onNodeWithText("ESP32").performClick()
        compose.onNodeWithText("Skip").performClick()
        assertEquals(listOf(setOf(BuildInterest.Esp32)), finished)
    }

    @Test
    fun startSavesTheChosenInterests() {
        val finished = mutableListOf<Set<BuildInterest>>()
        compose.setContent { PartloreTheme { OnboardingScreen(onFinish = { finished += it }, initialPage = 3) } }
        compose.onNodeWithText("ESP32").performClick()
        compose.onNodeWithText("Pico").performClick()
        compose.onNodeWithText("Start").performClick()
        assertEquals(listOf(setOf(BuildInterest.Esp32, BuildInterest.Pico)), finished)
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun everyInterestCanBeReachedOnASmallPhoneWithLargeText() {
        val finished = mutableListOf<Set<BuildInterest>>()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PartloreTheme { OnboardingScreen(onFinish = { finished += it }, initialPage = 3) }
            }
        }
        compose.onNodeWithText("Sensors").performScrollTo().performClick()
        compose.onNodeWithText("Start").performClick()
        assertEquals(listOf(setOf(BuildInterest.Sensors)), finished)
    }

    @Test
    fun continueSlidesToTheNextPage() {
        compose.setContent {
            PartloreTheme(motionPreference = MotionPreference.Full) { OnboardingScreen(onFinish = {}) }
        }
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Every pin has a source").assertIsDisplayed()
    }

    @Test
    fun continueCrossFadesToTheNextPageWithReducedMotion() {
        compose.setContent {
            PartloreTheme(motionPreference = MotionPreference.Reduced) { OnboardingScreen(onFinish = {}) }
        }
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Every pin has a source").assertIsDisplayed()
    }
}
