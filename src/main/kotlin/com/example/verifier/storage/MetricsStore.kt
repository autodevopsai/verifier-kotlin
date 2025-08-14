package com.example.verifier.storage

import com.example.verifier.model.Metric
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

class MetricsStore {
    private val metricsDir = File(".verifier", "metrics")

    fun record(metric: Metric) {
        try {
            metricsDir.mkdirs()
            val date = metric.timestamp.substring(0, 10)
            val file = File(metricsDir, "$date.json")
            val metrics = if (file.exists()) {
                val type = object : TypeToken<MutableList<Metric>>() {}.type
                Gson().fromJson(file.readText(), type)
            } else {
                mutableListOf<Metric>()
            }
            metrics.add(metric)
            file.writeText(Gson().toJson(metrics))
        } catch (e: Exception) {
            // logger.error("Failed to record metric", e)
        }
    }

    fun getMetrics(period: String): List<Metric> {
        return try {
            metricsDir.mkdirs()
            val now = Instant.now()
            val start = when (period) {
                "hourly" -> now.minus(1, ChronoUnit.HOURS)
                "daily" -> now.minus(1, ChronoUnit.DAYS)
                "weekly" -> now.minus(7, ChronoUnit.DAYS)
                "monthly" -> now.minus(30, ChronoUnit.DAYS)
                else -> now.minus(1, ChronoUnit.DAYS)
            }

            val files = metricsDir.listFiles { _, name -> name.endsWith(".json") } ?: emptyArray()
            val out = mutableListOf<Metric>()
            for (file in files) {
                val dateStr = file.nameWithoutExtension
                val fileDate = Instant.parse("$dateStr-T00:00:00Z")
                if (fileDate.isAfter(start.minus(1, ChronoUnit.DAYS))) { // Read one extra day to be safe
                    val content = file.readText()
                    val type = object : TypeToken<List<Metric>>() {}.type
                    val data: List<Metric> = Gson().fromJson(content, type)
                    out.addAll(data.filter { Instant.parse(it.timestamp).isAfter(start) })
                }
            }
            out
        } catch (e: Exception) {
            // logger.error("Failed to get metrics", e)
            emptyList()
        }
    }
}
