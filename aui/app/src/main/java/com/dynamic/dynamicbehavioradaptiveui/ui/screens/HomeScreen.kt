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
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptiveUIEngine
import com.dynamic.dynamicbehavioradaptiveui.llm.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

@Composable
fun HomeScreen() {
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
                useGPU = false  // Disable GPU on iQOO for battery optimization
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

    // Track screen open with timing
    val screenOpenStart = remember { systemContext.nanotime() }

    // Debounced behavioral event processing
    val behavioralEvents by remember { mutableStateOf(mutableListOf<BehavioralEvent>()) }
    var lastEventTime by remember { 0L }

    // Adaptive inference frequency control
    val adaptiveFrequency by remember { mutableStateOf(2000L) }
    var lastInferenceTime by remember { 0L }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic UI", style = MaterialTypography.titleLarge) },
                actions = {
                    OutlinedButton(onClick = {}) {
                        Text("Settings")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.width(150.dp).padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("3", style = MaterialTypography.displayMedium)
                        Text("Tasks", style = MaterialTypography.bodySmall, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.width(150.dp).padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("2", style = MaterialTypography.displayMedium)
                        Text("Events", style = MaterialTypography.bodySmall, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.width(150.dp).padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("5", style = MaterialTypography.displayMedium)
                        Text("Notes", style = MaterialTypography.bodySmall, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.width(150.dp).padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("12", style = MaterialTypography.displayMedium)
                        Text("Files", style = MaterialTypography.bodySmall, color = Color.Gray)
                    }
                }
            }

            // Action cards
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                elevation = CardElevation.Medium
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            telemetry.trackScreenOpened("home", null, null, null)
                            telemetry.trackButtonClicked("home", "tasks_btn", "navigate_tasks", null)
                            adaptiveUIEngine.processBehavioralEvent(
                                BehavioralEvent(
                                    type = BehavioralEventType.SCREEN_TRANSITION,
                                    data = mapOf("screen" to "tasks")
                                )
                            )
                            adaptiveUIEngine.requestAdaptation("home", null)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Taskboard,
                            contentDescription = "Tasks"
                        )
                    }
                    Text("Tasks", style = MaterialTypography.titleMedium)
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.width(80.dp).padding(top = 4.dp)
                    ) {
                        Text("3 pending")
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            telemetry.trackScreenOpened("home", null, null, null)
                            telemetry.trackButtonClicked("home", "calendar_btn", "navigate_calendar", null)
                            adaptiveUIEngine.processBehavioralEvent(
                                BehavioralEvent(
                                    type = BehavioralEventType.SCREEN_TRANSITION,
                                    data = mapOf("screen" to "calendar")
                                )
                            )
                            adaptiveUIEngine.requestAdaptation("home", null)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.CalendarDays,
                            contentDescription = "Calendar"
                        )
                    }
                    Text("Calendar", style = MaterialTypography.titleMedium)
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.width(80.dp).padding(top = 4.dp)
                    ) {
                        Text("2 events")
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            telemetry.trackScreenOpened("home", null, null, null)
                            telemetry.trackButtonClicked("home", "notes_btn", "navigate_notes", null)
                            adaptiveUIEngine.processBehavioralEvent(
                                BehavioralEvent(
                                    type = BehavioralEventType.SCREEN_TRANSITION,
                                    data = mapOf("screen" to "notes")
                                )
                            )
                            adaptiveUIEngine.requestAdaptation("home", null)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.NoteText,
                            contentDescription = "Notes"
                        )
                    }
                    Text("Notes", style = MaterialTypography.titleMedium)
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.width(80.dp).padding(top = 4.dp)
                    ) {
                        Text("5 notes")
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            telemetry.trackScreenOpened("home", null, null, null)
                            telemetry.trackButtonClicked("home", "files_btn", "navigate_files", null)
                            adaptiveUIEngine.processBehavioralEvent(
                                BehavioralEvent(
                                    type = BehavioralEventType.SCREEN_TRANSITION,
                                    data = mapOf("screen" to "files")
                                )
                            )
                            adaptiveUIEngine.requestAdaptation("home", null)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Folder,
                            contentDescription = "Files"
                        )
                    }
                    Text("Files", style = MaterialTypography.titleMedium)
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.width(80.dp).padding(top = 4.dp)
                    ) {
                        Text("12 documents")
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            telemetry.trackScreenOpened("home", null, null, null)
                            telemetry.trackButtonClicked("home", "settings_btn", "navigate_settings", null)
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Sliders,
                            contentDescription = "Settings"
                        )
                    }
                    Text("Settings", style = MaterialTypography.titleMedium)
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.width(80.dp).padding(top = 4.dp)
                    ) {
                        Text("Appearance")
                    }
                }
            }
        }
    }

    // Post-compose: start adaptive monitoring
    DisposableEffect(Unit) {
        coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                awaitStartOfFrame()
                adaptiveUIEngine.observeAndAdapt("home", null)
                delay(adaptiveFrequency)
            }
        }
        onDispose { coroutineScope.cancel()}
    }
}