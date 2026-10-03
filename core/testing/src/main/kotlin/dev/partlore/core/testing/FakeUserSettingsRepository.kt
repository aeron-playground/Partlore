package dev.partlore.core.testing

import dev.partlore.core.model.BuildInterest
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory settings for tests. */
class FakeUserSettingsRepository(initial: UserSettings = UserSettings()) : UserSettingsRepository {
    override val settings = MutableStateFlow(initial)

    override suspend fun setTheme(theme: ThemeSetting) = settings.update { it.copy(theme = theme) }

    override suspend fun setMotion(motion: MotionSetting) = settings.update { it.copy(motion = motion) }

    override suspend fun setHaptics(enabled: Boolean) = settings.update { it.copy(haptics = enabled) }

    override suspend fun setSounds(enabled: Boolean) = settings.update { it.copy(sounds = enabled) }

    override suspend fun completeOnboarding(interests: Set<BuildInterest>) =
        settings.update { it.copy(onboardingDone = true, interests = interests) }
}
