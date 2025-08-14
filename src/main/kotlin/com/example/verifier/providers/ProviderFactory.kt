package com.example.verifier.providers

import com.example.verifier.model.Config

interface AiProvider {
    suspend fun complete(prompt: String, options: Map<String, Any> = emptyMap()): String
}

object ProviderFactory {
    suspend fun create(model: String, config: Config): AiProvider {
        // This will be implemented later.
        return object : AiProvider {
            override suspend fun complete(prompt: String, options: Map<String, Any>): String {
                return """
                    {
                        "risk_score": 0,
                        "vulnerabilities": [],
                        "summary": "No vulnerabilities found."
                    }
                """.trimIndent()
            }
        }
    }
}
