package dev.partlore.feature.settings

import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting

/** A change the user makes on the Settings screen. */
sealed interface SettingsChange {
    data class Theme(val theme: ThemeSetting) : SettingsChange

    data class Motion(val motion: MotionSetting) : SettingsChange

    data class Haptics(val enabled: Boolean) : SettingsChange

    data class Sounds(val enabled: Boolean) : SettingsChange
}
