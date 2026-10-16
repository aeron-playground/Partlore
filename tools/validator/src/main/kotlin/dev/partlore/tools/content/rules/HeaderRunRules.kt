package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Header
import dev.partlore.tools.content.model.HeaderRun

/** A header's runs place every position once, in a direction the drawing can follow. */
object HeaderRunRules {
    private val VERTICAL_ENDS = setOf("top", "bottom")
    private val HORIZONTAL_ENDS = setOf("left", "right")

    fun check(content: Content, d: Diagnostics) = content.parts.forEach { part ->
        val headers = part.pins?.headers.orEmpty()
        headers.forEach { header -> check(header, d) }
        checkSharedEdges(headers, d)
    }

    // Runs that share an edge, from one header or several (the Uno's IOH and IOL), are drawn in their
    // `order`; without one the drawing would have to guess.
    private fun checkSharedEdges(headers: List<Header>, d: Diagnostics) {
        headers
            .flatMap { header -> header.runs.map { header to it } }
            .groupBy { (_, run) -> run.edge }
            .filter { it.value.size > 1 }
            .forEach { (edge, runs) ->
                val where = if (edge == "inside") "inside the board" else "on the $edge edge"
                runs.filter { it.second.order == null }.map { it.first }.distinct().forEach { header ->
                    d.error(header.pos, "runs $where need an order each")
                }
                runs.filter { it.second.order != null }.groupBy { it.second.order }.values.filter { it.size > 1 }
                    .forEach { same -> d.error(same.last().first.pos, "two runs $where need different orders") }
            }
    }

    private fun check(header: Header, d: Diagnostics) {
        val covered = header.runs.sumOf { it.pins }
        if (covered != header.pinsPerRow) {
            d.error(header.pos, "runs of header ${header.id} cover $covered of ${header.pinsPerRow} positions")
        }
        header.runs.forEach { run -> runProblem(header, run)?.let { d.error(header.pos, it) } }
    }

    private fun runProblem(header: Header, run: HeaderRun): String? = directionProblem(run) ?: rowProblem(header, run)

    private fun isVertical(run: HeaderRun) =
        run.edge in HORIZONTAL_ENDS || (run.edge == "inside" && run.run == "vertical")

    private fun directionProblem(run: HeaderRun): String? {
        val vertical = isVertical(run)
        val ends = if (vertical) VERTICAL_ENDS else HORIZONTAL_ENDS
        return when {
            run.edge == "inside" && run.run == null -> "an inside run needs run: vertical or horizontal"

            run.edge != "inside" && run.run != null -> "run: is only for inside runs"

            run.first !in ends -> "a ${if (vertical) "vertical" else "horizontal"} run starts at ${ends.joinToString(
                " or ",
            )}"

            else -> null
        }
    }

    // Row 1 lies across the run: left/right of a vertical run, top/bottom of a horizontal one.
    private fun rowProblem(header: Header, run: HeaderRun): String? {
        val sides = if (isVertical(run)) HORIZONTAL_ENDS else VERTICAL_ENDS
        return when {
            header.rows == 1 && run.row1 != null -> "row1 is only for 2-row headers"
            header.rows == 2 && run.row1 !in sides -> "a 2-row header needs row1: ${sides.joinToString(" or ")}"
            else -> null
        }
    }
}
