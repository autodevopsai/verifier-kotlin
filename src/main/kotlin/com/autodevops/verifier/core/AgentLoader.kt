package com.autodevops.verifier.core

import com.autodevops.verifier.agents.LintAgent
import com.autodevops.verifier.agents.SecurityScanAgent
import com.autodevops.verifier.model.BaseAgent

object AgentLoader {
    fun loadAgents(): Map<String, BaseAgent> {
        val agents = listOf(
            LintAgent(),
            SecurityScanAgent()
        )
        return agents.associateBy { it.id }
    }
}
