package dev.partlore.core.testing

import dev.partlore.core.content.ContentRepository
import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartPage

/** Content from memory, for screen and ViewModel tests. Unknown IDs are NotFound. */
class FakeContentRepository(
    var library: ContentResult<LibraryHome> = ContentResult.Ok(SampleContent.library),
    val categories: MutableMap<String, ContentResult<CategoryPage>> =
        mutableMapOf(SampleContent.boards.id to ContentResult.Ok(SampleContent.boards)),
    val parts: MutableMap<String, ContentResult<PartPage>> =
        mutableMapOf(SampleContent.devBoard.id to ContentResult.Ok(SampleContent.devBoard)),
) : ContentRepository {
    override suspend fun library(): ContentResult<LibraryHome> = library

    override suspend fun category(id: String): ContentResult<CategoryPage> = categories[id] ?: ContentResult.NotFound

    override suspend fun part(id: String): ContentResult<PartPage> = parts[id] ?: ContentResult.NotFound
}
