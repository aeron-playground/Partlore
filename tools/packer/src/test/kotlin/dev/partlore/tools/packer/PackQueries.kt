package dev.partlore.tools.packer

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import java.io.File
import java.time.LocalDate

internal val TODAY: LocalDate = LocalDate.of(2026, 10, 4)

/** Every row of [sql] as text (NULL stays null). */
internal fun query(file: File, sql: String, vararg args: String): List<List<String?>> =
    BundledSQLiteDriver().open(file.path, SQLITE_OPEN_READONLY).use { db ->
        db.prepare(sql).use { st ->
            args.forEachIndexed { i, arg -> st.bindText(i + 1, arg) }
            buildList {
                while (st.step()) add((0 until st.getColumnCount()).map { if (st.isNull(it)) null else st.getText(it) })
            }
        }
    }

internal fun single(file: File, sql: String, vararg args: String): String? = query(file, sql, *args).single().single()
