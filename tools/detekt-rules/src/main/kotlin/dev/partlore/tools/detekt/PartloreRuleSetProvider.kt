package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/** The `partlore` rule set: design tokens only. detekt finds it through META-INF/services. */
class PartloreRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "partlore"

    override fun instance(config: Config): RuleSet =
        RuleSet(ruleSetId, listOf(RawColor(config), DpLiteral(config), TweenOutsideMotion(config)))
}
