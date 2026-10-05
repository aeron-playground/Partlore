package dev.partlore.feature.part

import java.net.URLEncoder

/** Links that open the browser. The app itself never goes online. */
internal object GithubLinks {
    private const val REPO = "https://github.com/aeron-playground/Partlore"

    /** A new content-error issue with the part ID and pack version filled in (field IDs from content_error.yml). */
    fun reportProblem(partId: String, packVersion: String): String =
        "$REPO/issues/new?template=content_error.yml&part_id=${encode(partId)}&pack_version=${encode(packVersion)}"

    /** The part's folder. Part IDs are paths ("maker/part"), so the slash stays. */
    fun editOnGithub(partId: String): String = "$REPO/tree/main/content/parts/$partId"

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
