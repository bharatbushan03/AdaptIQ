package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dynamic.dynamicbehavioradaptiveui.R
import com.dynamic.dynamicbehavioradaptiveui.storage.DataStorePreferences
import com.dynamic.dynamicbehavioradaptiveui.ui.screens.PrivacyScreen

@Composable
fun SettingsScreen(
    context: androidx.compose.runtime.Context = LocalContext.current
) {
    var theme by remember { mutableStateOf("Light") }
    var adaptiveOptIn by remember { mutableStateOf(DataStorePreferences.isAdaptiveEnabled(context)) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val appContext = remember { context }

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
            // Section header
            Text(
                text = "Appearance",
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Theme selector
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

            // Divider
            Divider(modifier = Modifier.height(1.dp).padding(vertical = 8.dp))

            // Section header
            Text(
                text = "Behavior",
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Adaptation mode toggle
            OutlinedButton(
                onClick = {
                    adaptiveOptIn = !adaptiveOptIn
                    DataStorePreferences.setAdaptiveEnabled(appContext, adaptiveOptIn)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text(
                        text = stringResource(R.string.adaptive_opt_in_enabled),
                        style = MaterialTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (adaptiveOptIn) {
                            androidx.compose.material.icons.FontAwesome5.Sun
                        } else {
                            androidx.compose.material.icons.FontAwesome5.Moon
                        },
                        contentDescription = if (adaptiveOptIn) "Adaptive UI enabled" else "Adaptive UI disabled"
                    )
                }
            }

            // Description text based on opt-in state
            Text(
                text = if (adaptiveOptIn)
                    "The app will adapt to your usage patterns to improve your experience. You can opt-out at any time."
                    else
                    "Adaptive UI is disabled. The app will use a default, non-personalized layout.",
                style = MaterialTypography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Demo mode toggle
            OutlinedButton(
                onClick = { DemoMode.stopDemo() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("Demo Mode", style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (DemoMode.isDemoMode()) {
                            androidx.compose.material.icons.FontAwesome5.Sun
                        } else {
                            androidx.compose.material.icons.FontAwesome5.Moon
                        },
                        contentDescription = "Toggle demo mode"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section header
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

            // Clear behavioral history action
            OutlinedButton(
                onClick = { showClearConfirmation = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("clear_history".provideAsString(), style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.Trash,
                        contentDescription = "Clear behavioral history"
                    )
                }
            }

            // Confirmation dialog for clearing history
            if (showClearConfirmation) {
                OutlinedButton(
                    onClick = {
                        runCanceled {
                            clearBehavioralHistory()
                            showClearConfirmation = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    HStack(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp
                    ) {
                        Text("Yes, clear history", style = MaterialTypography.bodyMedium, contentColor = androidx.compose.ui.graphics.Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Check,
                            contentDescription = "Confirmation"
                        )
                    }
                }
            }
        }
    }
}

private fun clearBehavioralHistory(appContext: androidx.compose.runtime.Context) {
    // Clear behavioral history from SharedPreferences
    DataStorePreferences.clearBehavioralHistory(appContext)
    // Reset adaptive opt-in to default (enabled)
    // Note: Behavior will restart from cold start after clearing
    // Individual event clearing is handled via SharedPreferences
}