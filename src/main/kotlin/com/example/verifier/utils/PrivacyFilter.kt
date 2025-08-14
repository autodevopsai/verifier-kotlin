package com.example.verifier.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive

class PrivacyFilter {

    private val patterns = mapOf(
        "SSN" to "\\b\\d{3}-\\d{2}-\\d{4}\\b".toRegex(),
        "EMAIL" to "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b".toRegex(),
        "CREDIT_CARD" to "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b".toRegex(),
        "IP_ADDRESS" to "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b".toRegex(),
        "API_KEY" to "\\b(sk|api|key|token|secret|password)[-_]?[a-zA-Z0-9]{20,}\\b".toRegex(RegexOption.IGNORE_CASE),
        "AWS_KEY" to "\\b(AKIA[0-9A-Z]{16})\\b".toRegex(),
        "GITHUB_TOKEN" to "\\b(ghp_[a-zA-Z0-9]{36})\\b".toRegex(),
        "PRIVATE_KEY" to "-----BEGIN (RSA |EC )?PRIVATE KEY-----[\\s\\S]*?-----END (RSA |EC )?PRIVATE KEY-----".toRegex()
    )

    private val sensitiveKeys = setOf(
        "password",
        "secret",
        "token",
        "api_key",
        "apiKey",
        "private_key",
        "privateKey",
        "access_token",
        "accessToken",
        "refresh_token",
        "refreshToken",
        "client_secret",
        "clientSecret"
    )

    private val logger = Logger.getLogger(javaClass.name)

    fun filter(text: String): String {
        var filtered = text
        var redactionCount = 0
        patterns.forEach { (type, pattern) ->
            val matches = pattern.findAll(filtered).count()
            if (matches > 0) {
                redactionCount += matches
                filtered = filtered.replace(pattern, "[REDACTED_${type}]")
            }
        }
        if (redactionCount > 0) {
            logger.debug { "Redacted $redactionCount sensitive patterns" }
        }
        return filtered
    }

    fun filterObject(obj: Any): Any {
        val gson = Gson()
        val jsonElement = gson.toJsonTree(obj)
        return gson.fromJson(filterJsonElement(jsonElement), Any::class.java)
    }

    private fun filterJsonElement(jsonElement: JsonElement): JsonElement {
        return when (jsonElement) {
            is JsonObject -> {
                val newObj = JsonObject()
                jsonElement.entrySet().forEach { (key, value) ->
                    newObj.add(key, if (isSensitiveKey(key)) JsonPrimitive("[REDACTED]") else filterJsonElement(value))
                }
                newObj
            }
            is JsonArray -> {
                val newArr = JsonArray()
                jsonElement.forEach { newArr.add(filterJsonElement(it)) }
                newArr
            }
            is JsonPrimitive -> if (jsonElement.isString) JsonPrimitive(filter(jsonElement.asString)) else jsonElement
            else -> jsonElement
        }
    }

    private fun isSensitiveKey(key: String): Boolean {
        return sensitiveKeys.any { it.equals(key, ignoreCase = true) }
    }
}
