package com.example.verifier.commands

import com.example.verifier.core.ConfigLoader
import com.example.verifier.model.Budgets
import com.example.verifier.model.Config
import com.example.verifier.model.Models
import com.example.verifier.model.Providers
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File

@Command(name = "init", description = ["Initialize verifier in this repo"])
class InitCommand : Runnable {

    @Option(names = ["--force", "-f"], description = ["Overwrite existing config"])
    private var force: Boolean = false

    override fun run() {
        val baseDir = File(".verifier")
        val configPath = File(baseDir, "config.yaml")

        if (configPath.exists() && !force) {
            println("Verifier already initialized. Use --force to overwrite.")
            return
        }

        baseDir.mkdirs()
        File(baseDir, "artifacts").mkdirs()
        File(baseDir, "sessions").mkdirs()
        File(baseDir, "metrics").mkdirs()
        File(baseDir, "logs").mkdirs()

        // Hardcoded answers
        val apiKey = "YOUR_API_KEY"
        val primaryModel = "gpt-4o-mini"

        val config = Config(
            models = Models(primary = primaryModel),
            providers = Providers(openai = com.example.verifier.model.ApiKey()),
            budgets = Budgets(),
            thresholds = com.example.verifier.model.Thresholds(),
            hooks = emptyMap()
        )

        ConfigLoader.save(config)

        val envVar = "OPENAI_API_KEY"
        val envContent = "# Verifier CLI\n# Add additional env here\n$envVar=$apiKey\n"
        val envFile = File(baseDir, ".env")
        envFile.writeText(envContent)
        envFile.setReadable(true, true)
        envFile.setWritable(true, true)

        println("✓ Verifier initialized at .verifier/")
        println("API key saved to .verifier/.env. Please ensure this file is in your .gitignore.")
    }
}
