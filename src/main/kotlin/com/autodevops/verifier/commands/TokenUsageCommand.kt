package com.autodevops.verifier.commands

import com.autodevops.verifier.core.ConfigLoader
import com.autodevops.verifier.storage.MetricsStore
import com.google.gson.GsonBuilder
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "token-usage", description = ["Show token usage"])
class TokenUsageCommand : Runnable {

    @Option(names = ["--period", "-p"], description = ["hourly|daily|weekly|monthly"], defaultValue = "daily")
    private lateinit var period: String

    @Option(names = ["--format", "-f"], description = ["table|json"], defaultValue = "table")
    private lateinit var format: String

    override fun run() {
        val metricsStore = MetricsStore()
        val config = ConfigLoader.load()
        val metrics = metricsStore.getMetrics(period)
        val usage = mutableMapOf<String, MutableMap<String, Number>>()
        var totalTokens = 0
        var totalCost = 0.0

        metrics.forEach {
            val current = usage.getOrPut(it.agentId) { mutableMapOf("tokens" to 0, "cost" to 0.0, "calls" to 0) }
            current["tokens"] = current["tokens"]!!.toInt() + (it.tokensUsed ?: 0)
            current["cost"] = current["cost"]!!.toDouble() + (it.cost ?: 0.0)
            current["calls"] = current["calls"]!!.toInt() + 1
            totalTokens += it.tokensUsed ?: 0
            totalCost += it.cost ?: 0.0
        }

        if (format == "json") {
            val out = mapOf(
                "period" to period,
                "total_tokens" to totalTokens,
                "total_cost" to totalCost,
                "by_agent" to usage
            )
            println(GsonBuilder().setPrettyPrinting().create().toJson(out))
            return
        }

        println("\nToken Usage Report ($period)")
        // Table formatting will be implemented later
        println("Total Tokens: $totalTokens")
        println("Total Cost: $totalCost")
    }
}
