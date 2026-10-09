package dev.partlore.core.content

import java.net.URLEncoder

/** Links that open the browser to fix content. The app itself never goes online. */
object ContentLinks {
    private const val REPO = "https://github.com/aeron-playground/Partlore"

    /** A new content-error issue with the part, pack version and (for one pin or spec) the field filled in. */
    fun reportProblem(partId: String, packVersion: String, fieldPath: String? = null): String =
        "$REPO/issues/new?template=content_error.yml&part_id=${encode(partId)}&pack_version=${encode(packVersion)}" +
            fieldPath?.let { "&field_path=${encode(it)}" }.orEmpty()

    /** The part's folder. Part IDs are paths ("maker/part"), so the slash stays. */
    fun editOnGithub(partId: String): String = "$REPO/tree/main/content/parts/$partId"

    // URLEncoder.encode(String, Charset) needs Android 13; the String charset version works everywhere.
    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
