package dev.partlore.tools.importer

/** The import can't go on without guessing. The message says what is missing or unknown. */
class ImportException(message: String, cause: Throwable? = null) : Exception(message, cause)
