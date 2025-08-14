package com.example.verifier.core

import com.example.verifier.model.AgentContext
import java.io.File

class ContextCollector {
    suspend fun collect(): AgentContext {
        return try {
            val branch = runCommand("git", "branch", "--show-current")?.trim()
            val diff = runCommand("git", "diff", "--cached")
            val files = runCommand("git", "status", "--porcelain")
                ?.lines()
                ?.mapNotNull { it.trim().split(" ").lastOrNull() }
            AgentContext(branch = branch, diff = diff, files = files)
        } catch (e: Exception) {
            // logger.warn("Git context unavailable; proceeding with minimal context")
            AgentContext()
        }
    }

    private fun runCommand(vararg command: String): String? {
        return try {
            val process = ProcessBuilder(*command)
                .directory(File("/app"))
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
    }
}
