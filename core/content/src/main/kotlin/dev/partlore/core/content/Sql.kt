package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

/** Every row of [sql], mapped by [row]. [args] bind as text, in order. */
internal fun <T> SQLiteConnection.query(sql: String, args: List<String> = emptyList(), row: (Columns) -> T): List<T> =
    prepare(sql).use { statement ->
        args.forEachIndexed { index, arg -> statement.bindText(index + 1, arg) }
        buildList { while (statement.step()) add(row(Columns(statement))) }
    }

/**
 * The columns of the current row, read left to right: each call reads the next column. Callers list
 * the reads in the same order as the SELECT, so no code counts column numbers.
 */
internal class Columns(private val statement: SQLiteStatement) {
    private var next = 0

    fun text(): String = statement.getText(next++)

    fun int(): Int = statement.getLong(next++).toInt()

    fun textOrNull(): String? = read { getText(it) }

    fun intOrNull(): Int? = read { getLong(it).toInt() }

    fun doubleOrNull(): Double? = read { getDouble(it) }

    private inline fun <T> read(value: SQLiteStatement.(Int) -> T): T? {
        val column = next++
        return if (statement.isNull(column)) null else statement.value(column)
    }
}
