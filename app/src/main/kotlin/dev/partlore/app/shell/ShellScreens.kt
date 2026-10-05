package dev.partlore.app.shell

import androidx.compose.runtime.Composable

/** What a page may ask the shell to do. Pages never see navigation keys (ADR 0008). */
class ShellNav(
    val back: () -> Unit,
    val openCategory: (id: String) -> Unit,
    val openPart: (id: String) -> Unit,
    val openSearch: () -> Unit,
)

/** The pages the shell shows. The app passes the real routes; tests pass stand-ins. */
class ShellScreens(
    val library: @Composable (nav: ShellNav) -> Unit,
    val category: @Composable (id: String, nav: ShellNav) -> Unit,
    val part: @Composable (id: String, nav: ShellNav) -> Unit,
    val settings: @Composable (nav: ShellNav) -> Unit,
)
