package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/** A colour written as a number, `Color(0xFF…)`, outside the theme: screens use the colour tokens. */
class RawColor(config: Config) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Style,
            "A raw colour outside the theme. Use a token from PartloreTheme.colors.",
            Debt.FIVE_MINS,
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "Color" && expression.valueArguments.any(::isHex)) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }

    // `Color(0xFF5B3FD9)` or `Color(color = 0x805B3FD9)`: a colour written as a hex number.
    private fun isHex(argument: KtValueArgument): Boolean =
        argument.getArgumentExpression()?.text.orEmpty().startsWith("0x", ignoreCase = true)
}
