package dev.partlore.app

import dev.partlore.app.shell.ShellScreens
import dev.partlore.feature.library.CategoryRoute
import dev.partlore.feature.library.LibraryHomeRoute
import dev.partlore.feature.part.PartRoute
import dev.partlore.feature.settings.SettingsRoute

/** The real pages, each with its ViewModel from Koin. */
internal fun appScreens(versionName: String): ShellScreens = ShellScreens(
    library = { nav -> LibraryHomeRoute(nav.openCategory, nav.openPart, nav.openSearch, appVersion = versionName) },
    category = { id, nav -> CategoryRoute(id, nav.back, nav.openPart, appVersion = versionName) },
    part = { id, nav -> PartRoute(id, nav.back, nav.openPart, appVersion = versionName) },
    settings = { nav -> SettingsRoute(versionName = versionName, onBack = nav.back) },
)
