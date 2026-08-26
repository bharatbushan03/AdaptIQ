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
import com.dynamic.dynamicbehavioradaptiveui.adaptation.BehavioralEvent
import com.dynamic.dynamicbehavioradaptiveui.llm.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

@Composable
fun NotesScreen() {
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

    var showAddNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    // Performance tracking
    val performanceSnapshot by adaptiveUIEngine.getPerformanceSnapshot()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes", style = MaterialTypography.titleLarge) },
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
                text = "My Notes",
                style = MaterialTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Performance info
            if (performanceSnapshot["adaptive"] == true) {
                Text(
                    text = "Adaptive active: ${performanceSnapshot["llmAvgLatencyMs"]}ms avg LLM latency",
                    style = MaterialTypography.bodySmall,
                    color = if (performanceSnapshot["llmAvgLatencyMs"] as Long < 2000) Color.Green else Color.Yellow
                )
            }

            if (showAddNote) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                OutlinedButton(
                    onClick = {
                        telemetry.trackButtonClicked("notes", "add_note_btn", "add_note", null)
                        telemetry.trackTaskCompletionTime("new_note", 0L, false, null)
                        showAddNote = false
                        noteText = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Note")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                                text = "Shopping list",
                                style = MaterialTypography.bodyMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(
                                onClick = {
                                    telemetry.trackFeatureUsageFrequency("notes_feature", 1, null)
                                    adaptiveUIEngine.processBehavioralEvent(
                                        BehavioralEvent(
                                            type = BehavioralEventType.FEATURE_USAGE,
                                            data = mapOf("feature" to "shopping_list")
                                        )
                                    )
                                    adaptiveUIEngine.requestAdaptation("notes", mapOf("feature" to "shopping_list"))
                                },
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                                text = "Ideas for app",
                                style = MaterialTypography.bodyMedium,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(
                                onClick = {
                                    telemetry.trackFeatureUsageFrequency("notes_feature", 2, null)
                                    adaptiveUIEngine.processBehavioralEvent(
                                        BehavioralEvent(
                                            type = BehavioralEventType.FEATURE_USAGE,
                                            data = mapOf("feature" to "ideas_for_app")
                                        )
                                    )
                                    adaptiveUIEngine.requestAdaptation("notes", mapOf("feature" to "ideas_for_app"))
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
                telemetry.trackButtonClicked("notes", "add_note_fab", "add_note", null)
                adaptiveUIEngine.processBehavioralEvent(
                    BehavioralEvent(
                        type = BehavioralEventType.SCREEN_TRANSITION,
                        data = mapOf("screen" to "notes", "action" to "add_note_fab")
                    )
                )
                adaptiveUIEngine.requestAdaptation("notes", mapOf("action" to "add_note_fab"))
                showAddNote = true
            }) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                    contentDescription = "Add Note"
                )
            }.align(Alignment.BottomEnd)
        }
    }
}