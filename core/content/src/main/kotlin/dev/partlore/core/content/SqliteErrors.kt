package dev.partlore.core.content

// What the SQLite driver throws: its own class on the JVM, Android's class on Android.
private val SQLITE_ERRORS = setOf("androidx.sqlite.SQLiteException", "android.database.SQLException")

/**
 * Runs [block]; when the SQLite driver fails, returns [onError] instead. androidx.sqlite's
 * SQLiteException is a class on the JVM but only another name for android.database.SQLException on
 * Android, so this module (built for the JVM) must not name it in a catch: the app would crash with
 * NoClassDefFoundError (see AndroidCompatibilityTest). Any other error is a bug and is thrown on.
 */
internal inline fun <T> catchingSqlite(block: () -> T, onError: (RuntimeException) -> T): T = try {
    block()
} catch (expected: RuntimeException) {
    if (expected.isSqliteError()) onError(expected) else throw expected
}

/** True for the driver's errors and their subclasses, checked by name so no class is loaded. */
internal fun Throwable.isSqliteError(): Boolean =
    generateSequence<Class<*>>(javaClass) { it.superclass }.any { it.name in SQLITE_ERRORS }
