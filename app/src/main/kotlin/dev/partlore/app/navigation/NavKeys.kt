package dev.partlore.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object LibraryHomeKey : NavKey

@Serializable data object SearchHomeKey : NavKey

@Serializable data object ToolsHomeKey : NavKey

@Serializable data object BenchHomeKey : NavKey

@Serializable data object SettingsKey : NavKey
