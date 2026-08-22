package com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels

import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.models.Note
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

class NotesViewModel(
    private val localStorage: LocalStorage = LocalStorage(),
) : ViewModel() {

    private val _notes = localStorage.notes.asFlow()
    val notes: Flow<List<Note>> = _notes.map { it.toList() }

    private val _isAddingNote = mutableLiveDataOf(false)
    val isAddingNote: androidx.lifecycle.MutableLiveData<Boolean> = _isAddingNote

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            _isAddingNote.value = true
            val note = Note(title = title, content = content)
            localStorage.addNote(note)
            telemetry.trackScreenOpened("notes", null, null, null)
            telemetry.trackButtonClicked("notes", "add_note_btn", "add_note", null)
            _isAddingNote.value = false
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            localStorage.removeNote(noteId)
            telemetry.trackWorkflowAbandonment(
                screen = "notes",
                workflowId = noteId,
                reason = "deleted_by_user",
                metadata = null
            )
        }
    }
}