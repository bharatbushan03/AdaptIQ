package com.dynamic.dynamicbehavioradaptiveui.storage

import com.dynamic.dynamicbehavioradaptiveui.models.Task
import com.dynamic.dynamicbehavioradaptiveui.models.Note
import com.dynamic.dynamicbehavioradaptiveui.models.CalendarEvent

// Mock local storage placeholder - using in-memory data for now
class LocalStorage {
    private var tasks = mutableListOf<Task>()
    private var notes = mutableListOf<Note>()
    private var calendarEvents = mutableListOf<CalendarEvent>()

    fun getTasks(): List<Task> = tasks
    fun addTask(task: Task) { tasks.add(task) }
    fun updateTask(task: Task) {
        val index = tasks.indexOf { it.id == task.id }
        if (index >= 0) { tasks[index] = task }
    }
    fun removeTask(taskId: String) { tasks.removeAll { it.id == taskId } }

    fun getNotes(): List<Note> = notes
    fun addNote(note: Note) { notes.add(note) }
    fun updateNote(note: Note) {
        val index = notes.indexOf { it.id == note.id }
        if (index >= 0) { notes[index] = note }
    }
    fun removeNote(noteId: String) { notes.removeAll { it.id == noteId } }

    fun getCalendarEvents(): List<CalendarEvent> = calendarEvents
    fun addEvent(event: CalendarEvent) { calendarEvents.add(event) }
    fun updateEvent(event: CalendarEvent) {
        val index = calendarEvents.indexOf { it.id == event.id }
        if (index >= 0) { calendarEvents[index] = event }
    }
    fun removeEvent(eventId: String) { calendarEvents.removeAll { it.id == eventId } }
}