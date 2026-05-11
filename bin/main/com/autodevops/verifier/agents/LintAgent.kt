package com.autodevops.verifier.agents

import com.autodevops.verifier.model.*
import com.google.gson.Gson
import java.io.File
import java.time.Instant

class LintAgent : BaseAgent {
    override val id = "lint"
    override val name = "Polyglot Linter"
    override val description = "Multi-language code linting"
    override val model = "none"
    override val maxTokens = 0

    override suspend fun execute(context: AgentContext): AgentResult {
        if (context.files.isNullOrEmpty()) {
            return createResult(PartialAgentResult(status = "skipped", error = "No files to lint"))
        }

        val issues = mutableListOf<Map<String, Any>>()
        var totalIssues = 0

        context.files.forEach { file ->
            val extension = File(file).extension.lowercase()
            if (extension.isEmpty()) return@forEach

            try {
                val lintResult = when (extension) {
                    "ts", "tsx", "js", "jsx" -> runESLint(file)
                    "py" -> runRuff(file)
                    "go" -> runGoFmt(file)
                    "rs" -> runRustFmt(file)
                    "java" -> runCheckstyle(file)
                    else -> null
                }

                if (lintResult != null) {
                    issues.add(mapOf("file" to file, "language" to getLanguage(extension), "issues" to lintResult))
                    totalIssues++
                }
            } catch (e: Exception) {
                // logger.debug("Linting failed for $file", e)
            }
        }

        val artifactsDir = File(".verifier", "artifacts")
        artifactsDir.mkdirs()
        val reportPath = File(artifactsDir, "lint-report.json")

        if (totalIssues > 0) {
            reportPath.writeText(Gson().toJson(issues))
        }

        val artifacts = if (totalIssues > 0) listOf(AgentArtifact("report", reportPath.path)) else emptyList()
        val severity = if (totalIssues > 10) AgentSeverity.WARNING else AgentSeverity.INFO

        return createResult(
            PartialAgentResult(
                data = mapOf("total_issues" to totalIssues.toString(), "files_checked" to context.files.size.toString(), "issues" to Gson().toJson(issues)),
                severity = severity,
                artifacts = artifacts
            )
        )
    }

    private fun runCommand(vararg command: String): String? {
        return try {
            val process = ProcessBuilder(*command).redirectErrorStream(true).start()
            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0) null else result
        } catch (e: Exception) {
            e.message
        }
    }

    private fun runESLint(file: String): String? = runCommand("npx", "eslint", file, "--format=json")
    private fun runRuff(file: String): String? = runCommand("ruff", "check", file, "--format=json")
    private fun runGoFmt(file: String): String? = runCommand("gofmt", "-d", file)
    private fun runRustFmt(file: String): String? = runCommand("rustfmt", "--check", file)
    private fun runCheckstyle(file: String): String? = runCommand("checkstyle", "-c", "/google_checks.xml", file)

    private fun getLanguage(extension: String): String {
        val map = mapOf("ts" to "TypeScript", "tsx" to "TypeScript React", "js" to "JavaScript", "jsx" to "JavaScript React", "py" to "Python", "go" to "Go", "rs" to "Rust", "java" to "Java")
        return map[extension] ?: extension.uppercase()
    }

    override fun createResult(partial: PartialAgentResult): AgentResult {
        return AgentResult(
            agentId = id,
            status = partial.status,
            error = partial.error,
            data = partial.data?.mapValues { it.value.toString() },
            severity = partial.severity,
            tokensUsed = partial.tokensUsed,
            cost = partial.cost,
            score = partial.score,
            artifacts = partial.artifacts,
            timestamp = Instant.now().toString()
        )
    }
}
