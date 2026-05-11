package com.autodevops.verifier.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val models: Models,
    val providers: Providers = Providers(),
    val budgets: Budgets,
    val thresholds: Thresholds = Thresholds(),
    val hooks: Map<String, List<String>> = emptyMap()
)

@Serializable
data class Models(
    val primary: String,
    val fallback: String? = null
)

@Serializable
data class Providers(
    val openai: ApiKey? = null
)

@Serializable
data class ApiKey(
    @SerialName("api_key")
    val apiKey: String? = null
)

@Serializable
data class Budgets(
    @SerialName("daily_tokens")
    val dailyTokens: Int = 100000,
    @SerialName("per_commit_tokens")
    val perCommitTokens: Int = 5000,
    @SerialName("monthly_cost")
    val monthlyCost: Int = 100
)

@Serializable
data class Thresholds(
    @SerialName("drift_score")
    val driftScore: Int = 30,
    @SerialName("security_risk")
    val securityRisk: Int = 5,
    @SerialName("coverage_delta")
    val coverageDelta: Int = -5
)
