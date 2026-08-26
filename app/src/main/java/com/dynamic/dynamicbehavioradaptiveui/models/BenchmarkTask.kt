package com.dynamic.dynamicbehavioradaptiveui.models

import java.time.Instant

data class BenchmarkTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val taskName: String = "",
    val mode: ExperimentMode,
    val setupInstructions: String = "",
    val successCriteria: String = "",
    val estimatedDurationMs: Long = 30000L,
    val completed: Boolean = false,
    val createdAt: Long = Instant.now().toEpochMilli(),
    var completedAt: Long? = null
)