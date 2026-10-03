package dev.partlore.app

import dev.partlore.core.designsystem.theme.MotionPreference
import dev.partlore.core.designsystem.theme.PartloreThemeMode
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeMappingTest {
    @Test
    fun systemThemeFollowsTheSystem() {
        assertEquals(PartloreThemeMode.Dark, ThemeSetting.System.toThemeMode(systemDark = true))
        assertEquals(PartloreThemeMode.Light, ThemeSetting.System.toThemeMode(systemDark = false))
    }

    @Test
    fun explicitThemesIgnoreTheSystem() {
        assertEquals(PartloreThemeMode.Light, ThemeSetting.Light.toThemeMode(systemDark = true))
        assertEquals(PartloreThemeMode.Dark, ThemeSetting.Dark.toThemeMode(systemDark = false))
        assertEquals(PartloreThemeMode.Bench, ThemeSetting.Bench.toThemeMode(systemDark = false))
    }

    @Test
    fun motionMapsOneToOne() {
        assertEquals(MotionPreference.System, MotionSetting.System.toMotionPreference())
        assertEquals(MotionPreference.Reduced, MotionSetting.Reduced.toMotionPreference())
        assertEquals(MotionPreference.Full, MotionSetting.Full.toMotionPreference())
    }
}
