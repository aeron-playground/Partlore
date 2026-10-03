package dev.partlore.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/** One destination in [PlScaffold]'s bottom bar or rail. */
@Immutable
data class PlNavItem(val label: String, @DrawableRes val icon: Int, val selected: Boolean, val onClick: () -> Unit)
