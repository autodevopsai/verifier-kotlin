package com.example.verifier.storage

import com.example.verifier.model.Session
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.Instant
import java.util.Date

class SessionStore {
    private val sessionsDir = File(".verifier", "sessions")

    fun save(session: Session) {
        try {
            val date = session.timestamp.substring(0, 10)
            val dir = File(sessionsDir, date)
            dir.mkdirs()
            val file = File(dir, "${session.hook}-${session.id}.json")
            file.writeText(Gson().toJson(session))
        } catch (e: Exception) {
            // logger.error("Failed to save session", e)
        }
    }

    fun get(sessionId: String): Session? {
        return try {
            findSessionFiles().forEach { file ->
                val content = file.readText()
                val session: Session = Gson().fromJson(content, object : TypeToken<Session>() {}.type)
                if (session.id == sessionId) {
                    return session
                }
            }
            null
        } catch (e: Exception) {
            // logger.error("Failed to get session", e)
            null
        }
    }

    fun list(since: Date? = null, hook: String? = null): List<Session> {
        return try {
            val files = findSessionFiles()
            val sessions = mutableListOf<Session>()
            for (file in files) {
                val content = file.readText()
                val session: Session = Gson().fromJson(content, object : TypeToken<Session>() {}.type)
                if (since != null && Instant.parse(session.timestamp).toEpochMilli() < since.time) continue
                if (hook != null && session.hook != hook) continue
                sessions.add(session)
            }
            sessions.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            // logger.error("Failed to list sessions", e)
            emptyList()
        }
    }

    private fun findSessionFiles(): List<File> {
        sessionsDir.mkdirs()
        val out = mutableListOf<File>()
        sessionsDir.listFiles()?.forEach { dateDir ->
            if (dateDir.isDirectory) {
                dateDir.listFiles { _, name -> name.endsWith(".json") }?.forEach { file ->
                    out.add(file)
                }
            }
        }
        return out
    }
}
