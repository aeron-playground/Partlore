package dev.partlore.core.userdata

import dev.partlore.core.model.BuildInterest
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** The user's settings, saved on the device. */
interface UserSettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setTheme(theme: ThemeSetting)

    suspend fun setMotion(motion: MotionSetting)

    suspend fun setHaptics(enabled: Boolean)

    suspend fun setSounds(enabled: Boolean)

    suspend fun completeOnboarding(interests: Set<BuildInterest>)

    suspend fun setPinLabel(mode: PinLabelMode)

    suspend fun setKeepScreenOn(on: Boolean)

    suspend fun setRedLight(on: Boolean)
}
