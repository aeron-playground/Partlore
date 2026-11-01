package dev.partlore.core.model

/** What someone typed, as the words the app looks for. */
data class SearchQuery(val words: List<String>) {
    companion object {
        /** Prefixes of pin names: "gpio 34" means the pin GPIO34, and a plain 34 can mean any of them. */
        val PIN_PREFIXES: List<String> = listOf("gpio", "gp", "io", "d", "a")

        private const val PIN_WORD = "pin"

        // A paste can be any size; every word costs a few more checks in each query.
        private const val MAX_WORDS = 6
        private const val MAX_WORD_LENGTH = 64
        private val SEPARATORS = Regex("[\\s\\p{Z}\\p{Cc}]+")

        fun parse(text: String): SearchQuery {
            val words = mutableListOf<String>()
            text.split(SEPARATORS).filter { it.isNotEmpty() }.forEach { raw ->
                val word = raw.lowercase()
                val previous = words.lastOrNull()
                when {
                    !isNumber(word) -> words += word
                    previous == PIN_WORD -> words[words.lastIndex] = word
                    previous in PIN_PREFIXES -> words[words.lastIndex] = previous + word
                    else -> words += word
                }
            }
            return SearchQuery(words.take(MAX_WORDS).map { it.take(MAX_WORD_LENGTH) })
        }

        fun isNumber(word: String): Boolean = word.isNotEmpty() && word.all { it in '0'..'9' }
    }
}

/** A part that matches every word. [alias] is the alias that matched, when the name and maker don't explain it. */
data class PartHit(
    val id: String,
    val name: String,
    val manufacturer: String,
    val kind: String,
    val level: VerificationLevel,
    val alias: String?,
)

/** A pin named by one word. [label] is the name that matched; [exact] means the whole name, not part of it. */
data class PinHit(
    val partId: String,
    val partName: String,
    val pinId: String,
    val label: String,
    val otherNames: List<String>,
    val exact: Boolean,
)

data class GotchaHit(
    val partId: String,
    val partName: String,
    val id: String,
    val title: String,
    val severity: Severity,
)

/** What one search found. Each list is cut at the same length; [more] says the pack holds more than they show. */
data class SearchResults(
    val parts: List<PartHit> = emptyList(),
    val pins: List<PinHit> = emptyList(),
    val gotchas: List<GotchaHit> = emptyList(),
    val more: Boolean = false,
) {
    val isEmpty: Boolean get() = parts.isEmpty() && pins.isEmpty() && gotchas.isEmpty()
}
