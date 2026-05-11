package com.autodevops.verifier.core

import com.autodevops.verifier.model.AgentContext
import com.autodevops.verifier.model.AgentResult
import com.autodevops.verifier.model.BaseAgent
import com.autodevops.verifier.model.Config
import com.autodevops.verifier.storage.MetricsStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant

class AgentRunner(private val config: Config, private val agents: Map<String, BaseAgent>) {

    private val metrics = MetricsStore()

    private fun getAgent(id: String): BaseAgent? {
        return agents[id]
    }

    suspend fun runAgent(id: String, context: AgentContext): AgentResult {
        try {
            val todays = metrics.getMetrics("daily")
            val used = todays.sumOf { it.tokensUsed ?: 0 }
            if (used >= (config.budgets.dailyTokens)) {
                return AgentResult(
                    agentId = id,
                    status = "skipped",
                    error = "Daily token budget exhausted",
                    timestamp = Instant.now().toString()
                )
            }
        } catch (e: Exception) {
            // Ignore
        }

        val agent = getAgent(id)
            ?: return AgentResult(
                agentId = id,
                status = "failure",
                error = "Agent $id not found",
                timestamp = Instant.now().toString()
            )

        val start = System.currentTimeMillis()
        return try {
            val result = agent.execute(context)
            metrics.record(
                com.autodevops.verifier.model.Metric(
                    agentId = id,
                    timestamp = result.timestamp,
                    tokensUsed = result.tokensUsed ?: 0,
                    cost = result.cost ?: 0.0,
                    result = result.status,
                    durationMs = System.currentTimeMillis() - start
                )
            )
            result
        } catch (err: Exception) {
            val now = Instant.now().toString()
            val result = AgentResult(
                agentId = id,
                status = "failure",
                error = "Agent execution failed",
                timestamp = now
            )
            metrics.record(
                com.autodevops.verifier.model.Metric(
                    agentId = id,
                    timestamp = now,
                    tokensUsed = 0,
                    cost = 0.0,
                    result = "failure",
                    durationMs = 0
                )
            )
            result
        }
    }

    suspend fun runMultiple(
        ids: List<String>,
        context: AgentContext,
        parallel: Boolean = true,
        failFast: Boolean = false
    ): List<AgentResult> = coroutineScope {
        if (parallel) {
            ids.map { async { runAgent(it, context) } }.awaitAll()
        } else {
            val out = mutableListOf<AgentResult>()
            for (id in ids) {
                val res = runAgent(id, context)
                out.add(res)
                if (failFast && res.status == "failure") break
            }
            out
        }
    }
}
