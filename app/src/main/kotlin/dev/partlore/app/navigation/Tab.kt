package dev.partlore.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import dev.partlore.app.R
import dev.partlore.core.designsystem.components.PlIcons

enum class Tab(val root: NavKey, @StringRes val label: Int, @DrawableRes val icon: Int) {
    Library(LibraryHomeKey, R.string.tab_library, PlIcons.Library),
    Search(SearchHomeKey, R.string.tab_search, PlIcons.Search),
    Tools(ToolsHomeKey, R.string.tab_tools, PlIcons.Tools),
    Bench(BenchHomeKey, R.string.tab_bench, PlIcons.Bench),
}
