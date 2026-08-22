package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels.TasksViewModel
import com.dynamic.dynamicbehavioradaptiveui.models.Task
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun TasksScreen(viewModel: TasksViewModel = viewModel()) {
    val vm = viewModel()
    val tasks by vm.tasks.collectAsList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks", style = MaterialTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Text(
                text = "My Tasks",
                style = MaterialTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            if (tasks.isEmpty()) {
                OutlinedButton(
                    onClick = { vm.addTask("New Task", "Enter task description") },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    HStack(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                            contentDescription = "Add Task"
                        )
                        Text("Add Task", style = MaterialTypography.bodyMedium)
                    }
                }
            } else {
                // Tasks list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTypography.titleMedium,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = task.description,
                                        style = MaterialTypography.bodySmall,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2
                                    )
                                }
                                if (task.completed) {
                                    ImageVector(
                                        imageVector = androidx.compose.material.icons.FontAwesome5.Check,
                                        contentDescription = "Completed"
                                    )
                                } else {
                                    OutlinedButton(
                                        onClick = { vm.toggleTaskCompletion(task) },
                                        modifier = Modifier.width(60.dp).height(32.dp)
                                    ) {
                                        Text("Done")
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating add button at bottom
                FloatingActionButton(onClick = { vm.addTask("New Task", "Enter description") }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                        contentDescription = "Add Task"
                    )
                }.align(Alignment.BottomEnd)
            }
        }
    }
}