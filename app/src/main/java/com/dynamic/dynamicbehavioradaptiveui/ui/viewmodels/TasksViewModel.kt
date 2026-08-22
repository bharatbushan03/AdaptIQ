package com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels

import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.models.Task
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

class TasksViewModel(
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
            telemetry.trackScreenOpened("tasks", null, null, null)
            telemetry.trackButtonClicked("tasks", "add_task_btn", "add_task", null)
            _isAddingTask.value = false
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            telemetry.trackButtonClicked("tasks", "toggle_completion", "toggle", null)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            localStorage.removeTask(taskId)
            telemetry.trackWorkflowAbandonment(
                screen = "tasks",
                workflowId = taskId,
                reason = "deleted_by_user",
                metadata = null
            )
        }
    }
}