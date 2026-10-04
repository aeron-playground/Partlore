package dev.partlore.tools.content.cli

import dev.partlore.tools.content.ship.Mode
import java.io.File

/** `<command> [--content <dir>] [--out <dir>] [--part <id>] [--preview]` */
data class CliOptions(
    val command: String,
    val content: File,
    val out: File?,
    val part: String?,
    val preview: Boolean,
) {
    /** Printed in front of each problem's path: the content folder as typed. */
    val prefix: String get() = content.path.trimEnd('/', '\\') + "/"

    val mode: Mode get() = if (preview) Mode.PREVIEW else Mode.RELEASE

    companion object {
        private val VALUED = setOf("--content", "--out", "--part")

        fun parse(args: Array<String>): CliOptions? {
            val flags = args.drop(1)
            val pairs = flags.filter { it != "--preview" }.chunked(2)
            val valid = pairs.all { it.size == 2 && it[0] in VALUED }
            val values = pairs.filter { it.size == 2 }.associate { it[0] to it[1] }
            return args.firstOrNull()?.takeIf { valid }?.let {
                CliOptions(
                    command = it,
                    content = File(values["--content"] ?: "content"),
                    out = values["--out"]?.let(::File),
                    part = values["--part"],
                    preview = "--preview" in flags,
                )
            }
        }
    }
}
