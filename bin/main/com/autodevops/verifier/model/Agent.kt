package com.autodevops.verifier.model

import kotlinx.serialization.Serializable

enum class AgentSeverity {
    INFO, WARNING, BLOCKING
}

@Serializable
data class AgentArtifact(
    val type: String,
    val path: String? = null,
    val content: String? = null
)

@Serializable
data class AgentResult(
    val agentId: String,
    val status: String,
    val error: String? = null,
    val data: Map<String, String>? = null,
    val severity: AgentSeverity? = null,
    val tokensUsed: Int? = null,
    val cost: Double? = null,
    val score: Int? = null,
    val artifacts: List<AgentArtifact>? = null,
    val timestamp: String
)

@Serializable
data class AgentContext(
    val repoPath: String? = null,
    val branch: String? = null,
    val diff: String? = null,
    val files: List<String>? = null,
    val env: Map<String, String>? = null,
    val additionalProperties: Map<String, String> = emptyMap()
)

interface BaseAgent {
    val id: String
    val name: String
    val description: String
    val model: String
    val maxTokens: Int

    fun createResult(partial: PartialAgentResult): AgentResult

    suspend fun execute(context: AgentContext): AgentResult
}

data class PartialAgentResult(
    val status: String = "success",
    val data: Map<String, String>? = null,
    val severity: AgentSeverity? = null,
    val tokensUsed: Int? = null,
    val cost: Double? = null,
    val error: String? = null,
    val score: Int? = null,
    val artifacts: List<AgentArtifact>? = null
)
