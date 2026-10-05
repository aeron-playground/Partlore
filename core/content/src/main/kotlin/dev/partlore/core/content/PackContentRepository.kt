package dev.partlore.core.content

import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Installs the pack on first use, opens it once and answers every read on one background thread
 * (a SQLite connection must not be shared between threads). A damaged pack is re-copied once.
 */
class PackContentRepository(
    private val install: (force: Boolean) -> InstallResult,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
) : ContentRepository {
    // Only touched on [dispatcher].
    private var reader: PackReader? = null

    override suspend fun library(): ContentResult<LibraryHome> = read { it.library() }

    override suspend fun category(id: String): ContentResult<CategoryPage> = read { it.category(id) }

    override suspend fun part(id: String): ContentResult<PartPage> = read { it.part(id) }

    private suspend fun <T : Any> read(query: (PackReader) -> T?): ContentResult<T> = withContext(dispatcher) {
        when (val opened = openReader()) {
            is ContentResult.Ok -> ask(opened.value, query)
            is ContentResult.Failed -> opened
            ContentResult.NotFound -> ContentResult.NotFound
        }
    }

    private fun <T : Any> ask(open: PackReader, query: (PackReader) -> T?): ContentResult<T> =
        catchingSqlite<ContentResult<T>>({ query(open)?.let { ContentResult.Ok(it) } ?: ContentResult.NotFound }) { e ->
            reader = null
            open.close()
            ContentResult.Failed(ContentProblem.Damaged, e.message.orEmpty())
        }

    private fun openReader(): ContentResult<PackReader> {
        reader?.let { return ContentResult.Ok(it) }
        val first = open(force = false)
        val result = if (first is ContentResult.Failed &&
            first.problem == ContentProblem.Damaged
        ) {
            open(force = true)
        } else {
            first
        }
        if (result is ContentResult.Ok) reader = result.value
        return result
    }

    private fun open(force: Boolean): ContentResult<PackReader> = when (val installed = install(force)) {
        is InstallResult.Ready -> PackReader.open(installed.file)
        is InstallResult.KeptPrevious -> PackReader.open(installed.file)
        is InstallResult.Failed -> ContentResult.Failed(installed.problem, installed.detail)
    }
}
