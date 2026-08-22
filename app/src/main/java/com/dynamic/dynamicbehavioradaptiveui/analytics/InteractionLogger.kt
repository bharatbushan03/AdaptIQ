package com.dynamic.dynamicbehavioradaptiveui.analytics

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import java.time.format.DateTimeFormatter
import java.timeInstant

object InteractionLogger {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    fun logEvent(event: InteractionEvent) {
        val metadataPreview = event.metadata.take(50)
        println("""
            |📱 InteractionEvent:
            |  eventId: ${event.eventId}
            |  timestamp: ${event.timestamp}
            |  sessionId: ${event.sessionId}
            |  screen: ${event.screen}
            |  action: ${event.action}
            |  target: ${event.target}
            |  previousScreen: ${event.previousScreen}
            |  duration: ${event.duration}ms
            |  workflowId: ${event.workflowId}
            |  success: ${event.success}
            |  metadata: ${metadataPreview}
            |""".trimMargin())
    }

    fun logEventBatch(events: List<InteractionEvent>) {
        println("""
            |📥 Batch of ${events.size} interaction events:
            |""".trimMargin())
        events.forEach { logEvent(it) }
        println("|--------------------------------------------------|")
    }

    fun debugSessionInfo(sessionId: String) {
        println("""
            |🔧 Session Debug:
            |  sessionId: $sessionId
            |  events captured: tracking active
            |  local storage: Room database
            |  transmission: offline (local only)
            |""".trimMargin())
    }
}