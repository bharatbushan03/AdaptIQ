package com.dynamic.dynamicbehavioradaptiveui.models

data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L
)

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String = "",
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class CalendarEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val allDay: Boolean = false
)