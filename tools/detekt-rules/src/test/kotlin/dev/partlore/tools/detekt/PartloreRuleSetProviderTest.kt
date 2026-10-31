package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.ServiceLoader

class PartloreRuleSetProviderTest {
    @Test
    fun detektFindsThePartloreRules() {
        // detekt finds rule sets the same way: through the META-INF/services file.
        val provider = ServiceLoader.load(RuleSetProvider::class.java).single { it.ruleSetId == "partlore" }
        val rules = provider.instance(Config.empty).rules.map { it.ruleId }
        assertEquals(listOf("RawColor", "DpLiteral", "TweenOutsideMotion"), rules)
    }
}
