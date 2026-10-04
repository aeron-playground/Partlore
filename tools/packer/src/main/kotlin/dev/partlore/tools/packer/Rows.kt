package dev.partlore.tools.packer

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

/** Inserts rows, preparing each table's statement once. Values go in the table's column order. */
internal class Rows(private val db: SQLiteConnection) : AutoCloseable {
    private val statements = mutableMapOf<String, SQLiteStatement>()

    fun insert(table: String, values: List<Any?>) {
        val statement =
            statements.getOrPut(table) {
                db.prepare("INSERT INTO $table VALUES (${values.joinToString(", ") { "?" }})")
            }
        values.forEachIndexed { i, value -> statement.bind(i + 1, value) }
        statement.step()
        statement.reset()
        statement.clearBindings()
    }

    override fun close() = statements.values.forEach { it.close() }
}

private fun SQLiteStatement.bind(index: Int, value: Any?) {
    when (value) {
        null -> bindNull(index)
        is String -> bindText(index, value)
        is Int -> bindLong(index, value.toLong())
        is Long -> bindLong(index, value)
        is Double -> bindDouble(index, value)
        is Boolean -> bindLong(index, if (value) 1L else 0L)
        else -> error("can't store a ${value::class.simpleName} in the pack")
    }
}
