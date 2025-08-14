package com.example.verifier.core

import com.charleskorn.kaml.Yaml
import com.example.verifier.model.Config
import java.io.File

object ConfigLoader {
    private val configPath = File(".verifier/config.yaml")

    fun load(): Config {
        if (!configPath.exists()) {
            throw IllegalStateException("Config file not found. Please run 'verifier init'.")
        }
        val text = configPath.readText()
        return Yaml.default.decodeFromString(Config.serializer(), text)
    }

    fun save(config: Config) {
        val text = Yaml.default.encodeToString(Config.serializer(), config)
        configPath.writeText(text)
    }
}
