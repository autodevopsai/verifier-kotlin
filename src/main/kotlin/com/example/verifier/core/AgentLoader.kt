package com.example.verifier.core

import com.example.verifier.agents.LintAgent
import com.example.verifier.agents.SecurityScanAgent
import com.example.verifier.model.BaseAgent

object AgentLoader {
    fun loadAgents(): Map<String, BaseAgent> {
        val agents = listOf(
            LintAgent(),
            SecurityScanAgent()
        )
        return agents.associateBy { it.id }
    }
}
