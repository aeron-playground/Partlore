package dev.partlore.core.content

import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartPage

/** What the screens read: one call per screen. */
interface ContentRepository {
    suspend fun library(): ContentResult<LibraryHome>

    suspend fun category(id: String): ContentResult<CategoryPage>

    suspend fun part(id: String): ContentResult<PartPage>
}
