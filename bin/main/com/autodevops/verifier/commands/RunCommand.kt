package com.autodevops.verifier.commands

import com.autodevops.verifier.core.AgentLoader
import com.autodevops.verifier.core.AgentRunner
import com.autodevops.verifier.core.ConfigLoader
import com.autodevops.verifier.core.ContextCollector
import kotlinx.coroutines.runBlocking
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "run", description = ["Run a verifier agent"])
class RunCommand : Runnable {

    @Parameters(index = "0", description = ["Agent id (lint, security-scan)"])
    private lateinit var agentId: String

    @Option(names = ["--files", "-f"], description = ["Files to run on"])
    private var files: List<String> = emptyList()

    override fun run() {
        runBlocking {
            val config = ConfigLoader.load()
            val agents = AgentLoader.loadAgents()
            if (!agents.containsKey(agentId)) {
                println("Agent '$agentId' not found.")
                println("Available agents: ${agents.keys.joinToString(", ")}")
                return@runBlocking
            }
            val runner = AgentRunner(config, agents)
            val contextCollector = ContextCollector()
            val repoContext = contextCollector.collect()
            val context = repoContext.copy(files = files)
            val result = runner.runAgent(agentId, context)
            if (result.status == "failure") {
                println("✗ $agentId failed: ${result.error}")
                return@runBlocking
            }
            println("✓ $agentId completed")
            result.severity?.let { println("Severity: $it") }
            result.score?.let { println("Score: $it") }
            result.data?.let { println("Data: $it") }
        }
    }
}
