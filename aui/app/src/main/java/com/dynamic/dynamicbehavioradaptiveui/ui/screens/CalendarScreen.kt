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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.system.measureTimeMillis

@Composable
fun CalendarScreen() {
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

    var selectedDate by remember { remember { LocalDate.now() } }
    var showAddEvent by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDesc by remember { mutableStateOf("") }

    // Performance tracking
    val performanceSnapshot by adaptiveUIEngine.getPerformanceSnapshot()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", style = MaterialTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    OutlinedButton(onClick = {
                        adaptiveUIEngine.cancelCurrentInference()
                    }) {
                        HStack(
                            modifier = Modifier.padding(4.dp),
                            contentPadding = 8.dp
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                                contentDescription = "Add Event"
                            )
                            Text("Add", style = MaterialTypography.bodySmall)
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "prev_month")
                        )
                    )
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.ChevronLeft,
                        contentDescription = "Previous month"
                    )
                }
                Text(
                    text = "${selectedDate.month.name.capitalize()}, ${selectedDate.year}",
                    style = MaterialTypography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "next_month")
                        )
                    )
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.ChevronRight,
                        contentDescription = "Next month"
                    )
                }
            }

            // Day grid headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"].forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTypography.bodyMedium,
                        modifier = Modifier.width(32.dp).height(32.dp),
                        horizontalAlignment = Alignment.Center
                    )
                }
            }

            // Simple calendar days (mock - 1st to 7th of month)
            var dayIndex by remember { mutableStateOf(1) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_1")
                        )
                    )
                }) {
                    Text("1", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_2")
                        )
                    )
                }) {
                    Text("2", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_3")
                        )
                    )
                }) {
                    Text("3", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_4")
                        )
                    )
                }) {
                    Text("4", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_5")
                        )
                    )
                }) {
                    Text("5", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_6")
                        )
                    )
                }) {
                    Text("6", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
                OutlinedButton(onClick = {
                    adaptiveUIEngine.processBehavioralEvent(
                        BehavioralEvent(
                            type = BehavioralEventType.SCREEN_TRANSITION,
                            data = mapOf("screen" to "calendar", "action" to "day_7")
                        )
                    )
                }) {
                    Text("7", style = MaterialTypography.titleSmall, horizontalAlignment = Alignment.Center)
                }
            }

            // Events for selected day
            if (showAddEvent) {
                OutlinedTextField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    label = { Text("Event title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = eventDesc,
                    onValueChange = { eventDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                OutlinedButton(
                    onClick = {
                        telemetry.trackButtonClicked("calendar", "save_event_btn", "save_event", null)
                        telemetry.trackTaskCompletionTime("new_event", 0L, false, null)
                        showAddEvent = false
                        eventTitle = ""
                        eventDesc = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Event")
                }
            } else {
                // Show event placeholder
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No events scheduled",
                            style = MaterialTypography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Center
                        )
                        OutlinedButton(
                            onClick = {
                                adaptiveUIEngine.processBehavioralEvent(
                                    BehavioralEvent(
                                        type = BehavioralEventType.SCREEN_TRANSITION,
                                        data = mapOf("screen" to "calendar", "action" to "add_event")
                                    )
                                )
                                adaptiveUIEngine.requestAdaptation("calendar", mapOf("action" to "add_event"))
                                showAddEvent = true
                            },
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text("Add Event")
                        }
                    }
                }
            }
        }
    }
}