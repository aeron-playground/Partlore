package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import dev.partlore.core.model.CategoryTile
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartCard
import dev.partlore.core.model.Tag
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.packformat.PackFormat
import java.io.File

/** Reads one content pack. Not thread-safe: [PackContentRepository] uses it from one thread at a time. */
class PackReader private constructor(private val db: SQLiteConnection) : AutoCloseable {
    fun library(): LibraryHome {
        val categories = categories()
        val counts = directCounts()
        return LibraryHome(
            isPreview = meta("preview") == "1",
            partCount = counts.values.sum(),
            starterBoards = cards("p.kind = 'board'", limit = STARTER_BOARDS),
            categories = categories.filter {
                it.parentId == null
            }.sortedWith(CATEGORY_ORDER).map { tile(it, categories, counts) },
        )
    }

    override fun close() = db.close()

    private fun meta(key: String): String? = db.query("SELECT value FROM meta WHERE key = ?", listOf(key)) {
        it.text()
    }.firstOrNull()

    private fun categories(): List<CategoryRow> = db.query("SELECT id, parent_id, name, sort FROM category") {
        CategoryRow(it.text(), it.textOrNull(), it.text(), it.intOrNull())
    }

    private fun directCounts(): Map<String, Int> =
        db.query("SELECT category_id, count(*) FROM part GROUP BY category_id") {
            it.text() to it.int()
        }
            .toMap()

    private fun tile(row: CategoryRow, all: List<CategoryRow>, counts: Map<String, Int>) =
        CategoryTile(row.id, row.name, subtree(row.id, all).sumOf { counts[it] ?: 0 })

    /** The category and everything below it. Guarded against parent loops. */
    private fun subtree(id: String, all: List<CategoryRow>, seen: MutableSet<String> = mutableSetOf()): List<String> =
        if (!seen.add(id)) {
            emptyList()
        } else {
            listOf(id) +
                all.filter { it.parentId == id }.flatMap { subtree(it.id, all, seen) }
        }

    /** Part cards matching [where] (SQL over `part p`), A–Z, with their status level and tags. */
    private fun cards(where: String, args: List<String> = emptyList(), limit: Int = Int.MAX_VALUE): List<PartCard> {
        val rows =
            db.query(
                "SELECT p.id, p.name, p.kind, p.category_id, s.level FROM part p " +
                    "JOIN status s ON s.part_id = p.id AND s.file = 'part' " +
                    "WHERE $where ORDER BY p.name COLLATE NOCASE, p.id LIMIT $limit",
                args,
            ) { CardRow(it.text(), it.text(), it.text(), it.text(), it.text()) }
        val tags =
            db.query(
                "SELECT pt.part_id, t.id, t.label FROM part_tag pt JOIN tag t ON t.id = pt.tag_id " +
                    "JOIN part p ON p.id = pt.part_id WHERE $where ORDER BY t.label COLLATE NOCASE",
                args,
            ) { it.text() to Tag(it.text(), it.text()) }
                .groupBy({ it.first }, { it.second })
        return rows.map { row ->
            PartCard(
                row.id,
                row.name,
                row.kind,
                row.categoryId,
                VerificationLevel.fromPack(row.level),
                tags[row.id].orEmpty(),
            )
        }
    }

    private data class CategoryRow(val id: String, val parentId: String?, val name: String, val sort: Int?)

    private data class CardRow(
        val id: String,
        val name: String,
        val kind: String,
        val categoryId: String,
        val level: String,
    )

    companion object {
        private const val STARTER_BOARDS = 12
        private val CATEGORY_ORDER = compareBy<CategoryRow>({ it.sort ?: Int.MAX_VALUE }, { it.name.lowercase() })

        /** Opens [file] read-only and checks that this app understands its format. */
        fun open(file: File): ContentResult<PackReader> {
            val db = if (file.isFile) connect(file) else null
            val version = db?.let(::schemaVersion)
            val problem =
                when {
                    !file.isFile -> ContentProblem.Missing
                    version == null -> ContentProblem.Damaged
                    version > PackFormat.SCHEMA_VERSION -> ContentProblem.TooNew
                    version < PackFormat.SCHEMA_VERSION -> ContentProblem.Damaged
                    else -> null
                }
            return if (problem == null && db != null) {
                ContentResult.Ok(PackReader(db))
            } else {
                db?.close()
                ContentResult.Failed(problem ?: ContentProblem.Damaged, describe(problem, file, version))
            }
        }

        private fun connect(file: File): SQLiteConnection? = try {
            BundledSQLiteDriver().open(file.path, SQLITE_OPEN_READONLY)
        } catch (_: SQLiteException) {
            null
        }

        private fun schemaVersion(db: SQLiteConnection): Int? = try {
            db.query("SELECT value FROM meta WHERE key = 'schema_version'") {
                it.text()
            }.firstOrNull()?.toIntOrNull()
        } catch (_: SQLiteException) {
            null
        }

        private fun describe(problem: ContentProblem?, file: File, version: Int?): String = when (problem) {
            ContentProblem.Missing -> "No content pack at ${file.path}"
            ContentProblem.TooNew -> "Pack format $version is newer than this app's ${PackFormat.SCHEMA_VERSION}"
            else -> "The content pack at ${file.path} can't be read (format ${version ?: "unknown"})"
        }
    }
}
