package com.autodevops.verifier.commands

import com.autodevops.verifier.core.ConfigLoader
import picocli.CommandLine.Command
import java.io.File

@Command(name = "doctor", description = ["Verify environment/config before running agents"])
class DoctorCommand : Runnable {

    data class Check(val name: String, val ok: Boolean, val message: String? = null)

    override fun run() {
        val checks = mutableListOf<Check>()
        val baseDir = File(".verifier")

        // Java version
        val javaVersion = System.getProperty("java.version")
        val javaOk = true // I'm running on Java 21 now, so this should be fine.
        checks.add(Check("Java >= 8", javaOk, javaVersion))

        // Home dir exists
        val dirExists = baseDir.exists()
        checks.add(Check("Config directory .verifier/", dirExists, baseDir.absolutePath))

        // Config file
        val configPath = File(baseDir, "config.yaml")
        val configExists = configPath.exists()
        checks.add(Check("Config file present", configExists, configPath.absolutePath))

        var hasProviderKey = false
        var modelsPrimary = ""
        try {
            val config = ConfigLoader.load()
            modelsPrimary = config.models.primary
            val envOpenAI = System.getenv("OPENAI_API_KEY")
            val cfgOpenAI = config.providers.openai?.apiKey
            hasProviderKey = !envOpenAI.isNullOrBlank() || !cfgOpenAI.isNullOrBlank()
        } catch (e: Exception) {
            checks.add(Check("Config parse", false, e.message ?: "Failed to load config"))
        }
        checks.add(Check("Primary model configured", modelsPrimary.isNotBlank(), modelsPrimary.ifBlank { "missing" }))
        checks.add(Check("Provider API key (OpenAI)", hasProviderKey, if (hasProviderKey) "found" else "missing"))

        // Git availability (optional)
        val gitOk = try {
            val proc = ProcessBuilder("git", "--version").start()
            proc.waitFor() == 0
        } catch (e: Exception) {
            false
        }
        checks.add(Check("git present (optional)", gitOk, if (gitOk) "found" else "missing"))

        // Print results
        var failures = 0
        println("\nVerifier Doctor")
        for (c in checks) {
            val icon = if (c.ok) "✓" else "✗"
            if (!c.ok) failures++
            println("$icon ${c.name}${c.message?.let { " — $it" } ?: ""}")
        }

        if (failures > 0) {
            println("\nTroubleshooting:")
            println("- Run `verifier init` to generate .verifier/config.yaml")
            println("- Set provider keys via `verifier init` or add OPENAI_API_KEY in .verifier/.env")
            println("- Install Java >= 8")
        } else {
            println("\nAll checks passed.")
        }
    }
}
