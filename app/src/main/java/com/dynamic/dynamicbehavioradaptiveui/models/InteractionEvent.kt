package com.dynamic.dynamicbehavioradaptiveui.models

import java.util.UUID
import java.time.Instant

data class InteractionEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val timestamp: Long = Instant.now().toEpochMilli(),
    val sessionId: String = UUID.randomUUID().toString(),
    val screen: String = "",
    val action: String = "",
    val target: String = "",
    val previousScreen: String = "",
    val duration: Long = 0L,
    val workflowId: String = UUID.randomUUID().toString(),
    val success: Boolean = true,
    val metadata: String = ""
)