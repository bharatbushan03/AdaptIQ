package com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels

import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.models.Task
import com.dynamic.dynamicbehavioradaptiveui.storage.LocalStorage
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.behavior.InitializedTelemetryRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.mutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sharing
import kotlinx.coroutines.launch

// Singleton telemetry repo - initialized once at app startup
val telemetry: BehaviorTracker = InitializedTelemetryRepository()

class HomeViewModel(
    private val localStorage: LocalStorage = LocalStorage(),
) : ViewModel() {

    private val _tasks = localStorage.tasks.asFlow()
    val tasks: Flow<List<Task>> = _tasks.map { it.toList() }

    private val _isAddingTask = mutableLiveDataOf(false)
    val isAddingTask: androidx.lifecycle.MutableLiveData<Boolean> = _isAddingTask

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            _isAddingTask.value = true
            val task = Task(title = title, description = description)
            localStorage.addTask(task)
            telemetry.trackScreenOpened("home", null, null, null)
            telemetry.trackButtonClicked("home", "add_task_btn", "add_task", null)
            telemetry.trackTaskCompletionTime(task.id, 0L, false, null)
            _isAddingTask.value = false
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            telemetry.trackButtonClicked("tasks", "toggle_completion", "toggle", null)
            telemetry.trackTaskCompletionTime(task.id, 1000L, !task.completed, null)
        }
    }

    suspend fun completeTask(task: Task, completed: Boolean) {
        val updated = task.copy(completed = completed)
        localStorage.updateTask(updated)
        telemetry.trackWorkflowCompletion(
            screen = "tasks",
            workflowId = task.id,
            success = completed,
            metadata = null
        )
    }
}