package dev.partlore.app

import dev.partlore.core.designsystem.theme.MotionPreference
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting

fun ThemeSetting.toThemeMode(systemDark: Boolean): PartloreThemeMode = when (this) {
    ThemeSetting.System -> if (systemDark) PartloreThemeMode.Dark else PartloreThemeMode.Light
    ThemeSetting.Light -> PartloreThemeMode.Light
    ThemeSetting.Dark -> PartloreThemeMode.Dark
    ThemeSetting.Bench -> PartloreThemeMode.Bench
}

fun MotionSetting.toMotionPreference(): MotionPreference = when (this) {
    MotionSetting.System -> MotionPreference.System
    MotionSetting.Reduced -> MotionPreference.Reduced
    MotionSetting.Full -> MotionPreference.Full
}
