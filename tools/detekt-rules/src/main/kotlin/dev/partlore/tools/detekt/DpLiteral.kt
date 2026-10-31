package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

/** A size written as a number, `16.dp`, outside the theme: screens use the spacing and layout tokens. */
class DpLiteral(config: Config) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Style,
            "A .dp number outside the theme. Use a token from PartloreTheme.spacing or PartloreLayout.",
            Debt.FIVE_MINS,
        )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.selectorExpression?.text == "dp" && isNumber(expression.receiverExpression)) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }

    // `16.dp`, `0.5.dp` or `(-4).dp`: a number, maybe in brackets or with a sign. `size.dp` is worked out, not raw.
    private fun isNumber(expression: KtExpression?): Boolean = when (expression) {
        is KtConstantExpression -> true
        is KtParenthesizedExpression -> isNumber(expression.expression)
        is KtPrefixExpression -> isNumber(expression.baseExpression)
        else -> false
    }
}
