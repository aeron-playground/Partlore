package dev.partlore.core.model

enum class ThemeSetting { System, Light, Dark, Bench }

enum class MotionSetting { System, Reduced, Full }

/** Answers to onboarding's "What do you build with?". */
enum class BuildInterest { Esp32, Arduino, Pico, Stm32, Sensors }

/** Everything the user chooses in Settings and onboarding. Defaults match a fresh install. */
data class UserSettings(
    val theme: ThemeSetting = ThemeSetting.System,
    val motion: MotionSetting = MotionSetting.System,
    val haptics: Boolean = true,
    val sounds: Boolean = false,
    val onboardingDone: Boolean = false,
    val interests: Set<BuildInterest> = emptySet(),
    /** The label mode the pinout viewer last used; null until the user picks one. */
    val pinLabel: PinLabelMode? = null,
    /** Keep the screen on in the pinout viewer; null follows the theme (on in Bench). */
    val keepScreenOn: Boolean? = null,
    val redLight: Boolean = false,
)
