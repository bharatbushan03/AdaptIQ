package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.behavior.telemetry
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptiveUIState
import com.dynamic.dynamicbehavioradaptiveui.llm.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

@Composable
fun SettingsScreen() {
    var theme by remember { mutableStateOf("Light") }
    var adaptiveUI by remember { mutableStateOf(false) }
    var adaptiveFrequency by remember { mutableStateOf(2000L) }
    var showPerformance by remember { mutableStateOf(false) }

    // Performance tracking state
    val adaptiveUIState = remember { AdaptiveUIState() }
    val performanceSnapshot by adaptiveUIEngine.getPerformanceSnapshot()

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
    val adaptiveUIEngine = remember {
        AdaptiveUIEngine(
            behaviorTracker = telemetryRepo,
            localLLMManager = llmManager,
            coroutineScope = coroutineScope
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTypography.titleLarge) },
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
            // Appearance section
            Text(
                text = "Appearance",
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = theme,
                onValueChange = { theme = it },
                label = { Text("Theme") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                trailingIcon = {
                    Icon(
                        imageVector = if (theme == "Light") {
                            androidx.compose.material.icons.FontAwesome5.Sun
                        } else {
                            androidx.compose.material.icons.FontAwesome5.Moon
                        },
                        contentDescription = "Toggle theme"
                    )
                }
            )

            // Adaptive behavior section
            OutlinedButton(
                onClick = {
                    telemetry.trackButtonClicked("settings", "adaptive_toggle", "toggle_adaptive", null)
                    adaptiveUI = !adaptiveUI
                    if (adaptiveUI) {
                        adaptiveUIEngine.observeAndAdapt("settings", mapOf("theme" to theme))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("Adaptive UI", style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (adaptiveUI) {
                            androidx.compose.material.icons.FontAwesome5.Check
                        } else {
                            androidx.compose.material.icons.FontAwesome5.Xmark
                        },
                        contentDescription = "Adaptive mode"
                    )
                }
            }

            // Adaptive frequency control
            if (adaptiveUI) {
                Text(
                    text = "Inference Frequency (ms)",
                    style = MaterialTypography.titleSmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = adaptiveFrequency.toString(),
                    onValueChange = { adaptiveFrequency = it.toLong() },
                    label = { Text("Adaptive frequency") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    trailingIcon = null
                )
            }

            Divider(modifier = Modifier.height(1.dp).padding(vertical = 8.dp))

            // Feature usage section
            Text(
                text = "Feature Usage Frequency",
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Track usage frequency events
            OutlinedButton(
                onClick = {
                    telemetry.trackFeatureUsageFrequency("tasks_feature", 15, null)
                    telemetry.trackFeatureUsageFrequency("calendar_feature", 8, null)
                    telemetry.trackFeatureUsageFrequency("notes_feature", 12, null)

                    // Process events with debounce and batching
                    adaptiveUIEngine.processBehavioralEvents()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("Refresh Stats", style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.ArrowRight,
                        contentDescription = "View stats"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Performance monitoring section
            OutlinedButton(
                onClick = {
                    showPerformance = !showPerformance
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text(
                        text = if (showPerformance) "Hide Performance" else "Show Performance",
                        style = MaterialTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showPerformance) {
                            androidx.compose.material.icons.FontAwesome5.ArrowDown
                        } else {
                            androidx.compose.material.icons.FontAwesome5.ArrowUp
                        },
                        contentDescription = "Toggle performance"
                    )
                }
            }

            if (showPerformance) {
                // Performance metrics card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Performance Metrics",
                            style = MaterialTypography.titleMedium
                        )

                        // Frame performance
                        val frameTime = performanceSnapshot["frameAvgTimeMs"] as Long
                        HStack(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 8.dp
                        ) {
                            Text(
                                text = "Frame time: ${frameTime}ms",
                                style = MaterialTypography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (frameTime < 16) { "✓" } else { "⚠" },
                                style = MaterialTypography.bodySmall
                            )
                        }

                        // LLM latency
                        val llmLatency = performanceSnapshot["llmAvgLatencyMs"] as Long
                        HStack(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 8.dp
                        ) {
                            Text(
                                text = "LLM latency: ${llmLatency}ms",
                                style = MaterialTypography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (llmLatency < 2000) { "✓ Responsive" } else { "⚠ Slow" },
                                style = MaterialTypography.bodySmall
                            )
                        }

                        // CPU usage
                        val cpuPercent = performanceSnapshot["cpuPercent"] as Float
                        HStack(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 8.dp
                        ) {
                            Text(
                                text = "CPU: ${cpuPercent}%",
                                style = MaterialTypography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ProgressBar(
                                value = cpuPercent / 100f,
                                modifier = Modifier.width(150.dp)
                            )
                        }

                        // Memory usage
                        val memoryMb = performanceSnapshot["memoryMb"] as Float
                        HStack(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 8.dp
                        ) {
                            Text(
                                text = "Memory: ${memoryMb:.1f} MB",
                                style = MaterialTypography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (memoryMb < 100) { "✓" } else { "⚠" },
                                style = MaterialTypography.bodySmall
                            )
                        }

                        // Adaptive status
                        Text(
                            text = "Adaptive UI: ${performanceSnapshot["adaptive"]}",
                            style = MaterialTypography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About section
            Text(
                text = "About",
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("Dynamic UI v1.0", style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.Info,
                        contentDescription = "Version info"
                    )
                }
            }
        }
    }
}