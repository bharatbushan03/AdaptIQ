package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptiveUIState
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.ProficiencyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.WorkflowFamiliarity
import com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels.*
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    // Fixed bottom navigation bar - does NOT move
    val selected by remember { mutableStateOf(0) }
    val adaptiveState = remember { AdaptiveUIState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic UI", style = MaterialTypography.titleLarge) },
                actions = {
                    OutlinedButton(onClick = { navController.navigate("settings") }) {
                        Text("Settings")
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
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Task card
            OutlinedButton(
                onClick = { navController.navigate("tasks") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Taskboard,
                    contentDescription = "Tasks",
                    tint = Color.Default
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tasks", style = MaterialTypography.titleMedium)
                    Text("3 pending", style = MaterialTypography.bodySmall, color = Color.Gray)
                }
            }

            // Calendar card
            OutlinedButton(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.CalendarDays,
                    contentDescription = "Calendar",
                    tint = Color.Default
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Calendar", style = MaterialTypography.titleMedium)
                    Text("2 events", style = MaterialTypography.bodySmall, color = Color.Gray)
                }
            }

            // Notes card
            OutlinedButton(
                onClick = { navController.navigate("notes") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.NoteText,
                    contentDescription = "Notes",
                    tint = Color.Default
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Notes", style = MaterialTypography.titleMedium)
                    Text("5 notes", style = MaterialTypography.bodySmall, color = Color.Gray)
                }
            }

            // Files card
            OutlinedButton(
                onClick = { navController.navigate("files") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Folder,
                    contentDescription = "Files",
                    tint = Color.Default
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Files", style = MaterialTypography.titleMedium)
                    Text("12 documents", style = MaterialTypography.bodySmall, color = Color.Gray)
                }
            }

            // Settings card
            OutlinedButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Sliders,
                    contentDescription = "Settings",
                    tint = Color.Default
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Settings", style = MaterialTypography.titleMedium)
                    Text("Appearance", style = MaterialTypography.bodySmall, color = Color.Gray)
                }
            }

            // Adaptive contextual shortcuts bar
            ContextualShortcutBar(
                state = adaptiveState,
                onShortcutSelected = { shortcut ->
                    // Handle shortcut selection
                    println("Shortcut selected: $shortcut")
                }
            )

            // Adaptive action group (shows based on behavior)
            AdaptiveActionGroup(
                state = adaptiveState,
                actions = listOf(
                    AdaptiveAction(
                        label: "New Event",
                        iconName: "calendar-event",
                        contentDescription: "Create new calendar event",
                        isAvailableFor: { behaviorState ->
                            behaviorState.currentIntent == "repeated_workflow" &&
                            behaviorState.proficiencyLevel != ProficiencyLevel.BEGINNER
                        }
                    ),
                    AdaptiveAction(
                        label: "New Task",
                        iconName: "task-new",
                        contentDescription: "Create new task",
                        isAvailableFor: { behaviorState ->
                            behaviorState.currentIntent == "repeated_workflow" ||
                            behaviorState.workflowFamiliarity == WorkflowFamiliarity.FAMILIAR
                        }
                    )
                )
            )

            // Contextual guidance card (appears on high friction)
            ContextualGuidanceCard(
                state = adaptiveState,
                onDismiss = { /* dismiss card */ }
            )

            // Feature recommendation
            FeatureRecommendation(
                state = adaptiveState,
                availableFeatures = listOf(
                    FeatureRecommendationFeature(
                        label: "Quick Add",
                        iconName: "task-quick",
                        contentDescription: "Quick task creation",
                        intermediateSuitable: true,
                        advancedSuitable: false
                    ),
                    FeatureRecommendationFeature(
                        label: "Calendar Integration",
                        iconName: "calendar-event",
                        contentDescription: "Sync with calendar",
                        intermediateSuitable: true,
                        advancedSuitable: true
                    ),
                    FeatureRecommendationFeature(
                        label: "Task Templates",
                        iconName: "create",
                        contentDescription: "Use task templates",
                        intermediateSuitable: false,
                        advancedSuitable: true
                    )
                ),
                onFeatureSelected = { feature ->
                    println("Feature selected: ${feature.label}")
                }
            )

            // Information density controller
            InformationDensityController(
                state = adaptiveState,
                onDensityChanged { density ->
                    println("Density changed to: $density")
                },
                currentDensity: 120
            )
        }
    }
}