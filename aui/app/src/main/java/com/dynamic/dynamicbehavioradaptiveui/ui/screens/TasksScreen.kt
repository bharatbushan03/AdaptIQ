package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.storage.DataStorePreferences
import com.dynamic.dynamicbehavioradaptiveui.behavior.telemetry
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptiveUIEvent
import com.dynamic.dynamicbehavioradaptiveui.llm.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

@Composable
fun TasksScreen() {
    val coroutineScope = rememberCoroutineScope()
    val adaptiveUIState = remember { AdaptiveUIState() }

    // Initialize telemetry repository
    val telemetryRepo = remember {
        InitializedTelemetryRepository(
            interactionEventDao = remember { /* would be injected */ },
            experimentSessionDao = remember { /* would be injected */ },
            experimentMetricsDao = remember { /* would be injected */ }
        )
    }

    // Initialize LLM manager
    val llmManager = remember {
        val llm = remember {
            TFLiteLocalLLMProvider(
                modelAssetPath = "tflite-model.tflite",
                useGPU = false
            )
        }
        LocalLLMManager(
            localLLM = llm,
            behaviorTracker = telemetryRepo,
            coroutineScope = coroutineScope
        )
    }

    // Initialize adaptive UI engine
    val adaptiveOptIn = DataStorePreferences.isAdaptiveEnabled(LocalContext.current)
    val adaptiveUIEngine = remember {
        AdaptiveUIEngine(
            behaviorTracker = telemetryRepo,
            localLLMManager = llmManager,
            coroutineScope = coroutineScope,
            adaptiveOptIn = adaptiveOptIn
        )
    }

    var showAddTask by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }

    // Performance tracking
    val performanceSnapshot by adaptiveUIEngine.getPerformanceSnapshot()

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
            Text(
                text = "My Tasks",
                style = MaterialTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Performance info (debug)
            if (performanceSnapshot["adaptive"] == true) {
                Text(
                    text = "Adaptive: ${performanceSnapshot["llmAvgLatencyMs"]}ms avg LLM",
                    style = MaterialTypography.bodySmall,
                    color = if (performanceSnapshot["llmAvgLatencyMs"] as Long < 2000) Color.Green else Color.Yellow
                )
            }

            if (showAddTask) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = taskDesc,
                    onValueChange = { taskDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                OutlinedButton(
                    onClick = {
                        telemetry.trackButtonClicked("tasks", "add_task_btn", "add_task", null)
                        telemetry.trackTaskCompletionTime("new_task", 0L, false, null)
                        showAddTask = false
                        taskText = ""
                        taskDesc = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Task")
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "New Task",
                                style = MaterialTypography.titleMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(
                                onClick = {
                                    telemetry.trackButtonClicked("tasks", "add_task_fab", "add_task", null)
                                    adaptiveUIEngine.processBehavioralEvent(
                                        BehavioralEvent(
                                            type = BehavioralEventType.SCREEN_TRANSITION,
                                            data = mapOf("screen" to "tasks", "action" to "add_task")
                                        )
                                    )
                                    adaptiveUIEngine.requestAdaptation("tasks", mapOf("action" to "add_task"))
                                    showAddTask = true
                                },
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }

                // Sample tasks
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pay bills",
                                style = MaterialTypography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    telemetry.trackButtonClicked("tasks", "pay_bills", "task_complete", null)
                                    adaptiveUIEngine.processBehavioralEvent(
                                        BehavioralEvent(
                                            type = BehavioralEventType.TASK_COMPLETION,
                                            data = mapOf("task" to "pay_bills", "success" to true)
                                        )
                                    )
                                    adaptiveUIEngine.requestAdaptation("tasks", mapOf("task" to "pay_bills"))
                                },
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Finish report",
                                style = MaterialTypography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    telemetry.trackButtonClicked("tasks", "finish_report", "task_complete", null)
                                    adaptiveUIEngine.processBehavioralEvent(
                                        BehavioralEvent(
                                            type = BehavioralEventType.TASK_COMPLETION,
                                            data = mapOf("task" to "finish_report", "success" to false)
                                        )
                                    )
                                    adaptiveUIEngine.requestAdaptation("tasks", mapOf("task" to "finish_report"))
                                },
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }
            }

            FloatingActionButton(onClick = {
                telemetry.trackButtonClicked("tasks", "add_task_fab", "add_task", null)
                adaptiveUIEngine.processBehavioralEvent(
                    BehavioralEvent(
                        type = BehavioralEventType.SCREEN_TRANSITION,
                        data = mapOf("screen" to "tasks", "action" to "add_task_fab")
                    )
                )
                adaptiveUIEngine.requestAdaptation("tasks", mapOf("action" to "add_task_fab"))
                showAddTask = true
            }) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                    contentDescription = "Add Task"
                )
            }.align(Alignment.BottomEnd)
        }
    }
}