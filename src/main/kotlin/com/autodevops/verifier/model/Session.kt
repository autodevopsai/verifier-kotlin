package com.autodevops.verifier.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val timestamp: String,
    val hook: String,
    val context: AgentContext,
    val results: List<AgentResult>,
    val metadata: Map<String, String>
)
