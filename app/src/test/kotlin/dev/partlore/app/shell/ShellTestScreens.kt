package dev.partlore.app.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.model.ContentResult
import dev.partlore.core.testing.SampleContent
import dev.partlore.feature.library.CategoryScreen
import dev.partlore.feature.library.CategoryState
import dev.partlore.feature.library.LibraryHomeScreen
import dev.partlore.feature.part.PartScreen

/** Stand-in pages that say which page is open and offer a button to move on. */
internal fun fakeScreens(settings: @Composable (ShellNav) -> Unit = { Text("Settings page") }) = ShellScreens(
    library = { nav -> PlButton("Open boards", onClick = { nav.openCategory("boards") }) },
    category = { id, nav ->
        Column {
            Text("Category $id")
            PlButton("Open the dev board", onClick = { nav.openPart("example/devboard-v1") })
        }
    },
    part = { id, nav ->
        Column {
            Text("Part $id")
            PlButton("Go back", onClick = nav.back)
        }
    },
    settings = settings,
)

/** The real screens with sample content, for screenshots. */
internal fun sampleScreens() = ShellScreens(
    library = { nav ->
        LibraryHomeScreen(ContentResult.Ok(SampleContent.library), nav.openCategory, nav.openPart, nav.openSearch)
    },
    category = { _, nav ->
        CategoryScreen(ContentResult.Ok(CategoryState(SampleContent.boards)), nav.back, nav.openPart, onAction = {})
    },
    part = { _, nav -> PartScreen(ContentResult.Ok(SampleContent.devBoard), nav.back, nav.openPart) },
    settings = {},
)
