package com.example.verifier.agents

import com.example.verifier.core.ConfigLoader
import com.example.verifier.model.*
import com.example.verifier.providers.ProviderFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

class SecurityScanAgent : BaseAgent {
    override val id = "security-scan"
    override val name = "Security Scanner"
    override val description = "Scans code for security vulnerabilities"
    override val model = "gpt-4o-mini"
    override val maxTokens = 2500

    override suspend fun execute(context: AgentContext): AgentResult {
        if (context.diff.isNullOrBlank()) {
            return createResult(PartialAgentResult(status = "skipped", error = "No diff available"))
        }

        val config = ConfigLoader.load()
        val provider = ProviderFactory.create(model, config)
        val prompt = "Analyze the following code diff for security vulnerabilities.\n\n${context.diff}\n\nRespond JSON with { \"risk_score\": 0, \"vulnerabilities\": [{\"type\":\"\",\"severity\":\"critical|high|medium|low\",\"description\":\"\",\"location\":\"\",\"recommendation\":\"\"}], \"summary\":\"\" }"

        return try {
            val response = provider.complete(prompt, mapOf("json_mode" to true, "system_prompt" to "You are a security expert analyzing code for vulnerabilities. Be thorough but avoid false positives.", "max_tokens" to 1200))
            val analysis: Map<String, Any> = try {
                Gson().fromJson(response, object : TypeToken<Map<String, Any>>() {}.type)
            } catch (e: Exception) {
                mapOf("risk_score" to 3, "vulnerabilities" to emptyList<Any>(), "summary" to response)
            }

            val vulnerabilities = analysis["vulnerabilities"] as? List<Map<String, Any>>
            val hasBlocking = vulnerabilities?.any { it["severity"] == "critical" || it["severity"] == "high" } == true
            val riskScore = (analysis["risk_score"] as? Double)?.toInt() ?: 0

            createResult(
                PartialAgentResult(
                    score = riskScore,
                    data = analysis.mapValues { it.value.toString() },
                    severity = if (hasBlocking) AgentSeverity.BLOCKING else if (riskScore > 5) AgentSeverity.WARNING else AgentSeverity.INFO,
                    tokensUsed = 2000,
                    cost = 0.08
                )
            )
        } catch (e: Exception) {
            createResult(PartialAgentResult(status = "failure", error = e.message ?: "Security scan failed"))
        }
    }

    override fun createResult(partial: PartialAgentResult): AgentResult {
        return AgentResult(
            agentId = id,
            status = partial.status,
            error = partial.error,
            data = partial.data,
            severity = partial.severity,
            tokensUsed = partial.tokensUsed,
            cost = partial.cost,
            score = partial.score,
            artifacts = partial.artifacts,
            timestamp = Instant.now().toString()
        )
    }
}
