package com.vela.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

class VelaRuleSetProvider : RuleSetProviderV3(RuleSetId("vela")) {
    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        RuleProvider { AssignmentExpressionWrappingRule() },
        RuleProvider { ConstructorAnnotationWrappingRule() },
    )
}
