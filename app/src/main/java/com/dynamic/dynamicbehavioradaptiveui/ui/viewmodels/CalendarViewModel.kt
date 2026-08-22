package com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels

import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.models.CalendarEvent
import com.dynamic.dynamicbehavioradaptiveui.storage.LocalStorage
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.behavior.telemetry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.mutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sharing
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val localStorage: LocalStorage = LocalStorage(),
) : ViewModel() {

    private val _events = localStorage.calendarEvents.asFlow()
    val events: Flow<List<CalendarEvent>> = _events.map { it.toList() }

    private val _isAddingEvent = mutableLiveDataOf(false)
    val isAddingEvent: androidx.lifecycle.MutableLiveData<Boolean> = _isAddingEvent

    fun addEvent(title: String, description: String, startTime: Long, endTime: Long, allDay: Boolean) {
        viewModelScope.launch {
            _isAddingEvent.value = true
            val event = CalendarEvent(
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                allDay = allDay
            )
            localStorage.addEvent(event)
            telemetry.trackScreenOpened("calendar", null, null, null)
            telemetry.trackButtonClicked("calendar", "add_event_btn", "add_event", null)
            _isAddingEvent.value = false
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            localStorage.removeEvent(eventId)
            telemetry.trackWorkflowAbandonment(
                screen = "calendar",
                workflowId = eventId,
                reason = "deleted_by_user",
                metadata = null
            )
        }
    }
}