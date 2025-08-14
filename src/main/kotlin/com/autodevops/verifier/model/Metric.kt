package com.autodevops.verifier.model

import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val agentId: String,
    val timestamp: String, // ISO string
    val tokensUsed: Int,
    val cost: Double,
    val result: String,
    val durationMs: Long
)
