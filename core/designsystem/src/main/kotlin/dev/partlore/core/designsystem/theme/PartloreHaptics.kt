package dev.partlore.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** App events that give touch feedback. The system haptics setting still applies. */
enum class PartloreHaptic(internal val type: HapticFeedbackType) {
    PinTap(HapticFeedbackType.SegmentTick),
    ToggleOn(HapticFeedbackType.ToggleOn),
    ToggleOff(HapticFeedbackType.ToggleOff),
    Confirm(HapticFeedbackType.Confirm),
    Reject(HapticFeedbackType.Reject),
}

class PartloreHaptics internal constructor(private val feedback: HapticFeedback, private val enabled: Boolean) {
    fun perform(event: PartloreHaptic) {
        if (enabled) feedback.performHapticFeedback(event.type)
    }
}

@Composable
fun rememberPartloreHaptics(enabled: Boolean = true): PartloreHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback, enabled) { PartloreHaptics(feedback, enabled) }
}
