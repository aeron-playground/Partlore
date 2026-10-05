package dev.partlore.core.content

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class AndroidCompatibilityTest {
    // androidx.sqlite's SQLiteException is a class on the JVM, where this module is built, but only another
    // name for android.database.SQLException on Android. A class that names it crashes the app on load.
    @Test
    fun noClassNamesTheJvmOnlySqliteException() {
        val classes = File(PackReader::class.java.protectionDomain.codeSource.location.toURI())
        val offenders =
            classes
                .walk()
                .filter { it.extension == "class" }
                .filter { JVM_ONLY_CLASS in it.readBytes().toString(Charsets.ISO_8859_1) }
                .map { it.name }
                .sorted()
                .toList()
        assertEquals(emptyList<String>(), offenders)
    }

    private companion object {
        const val JVM_ONLY_CLASS = "androidx/sqlite/SQLiteException"
    }
}
