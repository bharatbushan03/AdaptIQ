package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun PrivacyScreen(
    onBack: () -> Unit = { },
    adaptiveOptIn: Boolean = true,
    onAdaptiveOptInChanged: (Boolean) -> Unit = { },
    clearHistory: () -> Unit = { }
) {
    var adaptiveOpt by remember { mutableStateOf(adaptiveOptIn) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy", style = MaterialTypography.titleLarge) },
                leadingIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // What is collected section
Text(
                text = getString(R.string.privacy_what_collected),
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

        Text(
            text = """
               • Behavioral interaction events (screen navigations, button clicks, feature usage)
               • Timing and duration data (dwell time, interaction intervals)
               • Workflow patterns and navigation sequences
               • Error and success status per action
               • Feature usage frequency and order

               All data is anonymized and stored locally on your device. No personal identifiers
               (such as name, email, or user ID) are collected or stored.
            """.trimMargin(),
            style = MaterialTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Why it is collected section
Text(
                text = getString(R.string.privacy_why_collected),
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

        Text(
            text = """• To power adaptive UI improvements that match your workflow patterns
                • To identify frequently used features and prioritize them
                • To detect and reduce interaction friction
                • To personalize the adaptive experience without compromising privacy
                • All analysis occurs on-device to protect your privacy""".trimMargin(),
            style = MaterialTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Where it is processed section
Text(
                text = getString(R.string.privacy_where_processed),
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

        Text(
            text = """• All behavioral data is processed on your device only
                • Local TensorFlow Lite model for LLM-based adaptations runs inference locally
                • No data is transmitted to external servers or cloud services
                • The Room SQLite database resides exclusively on your device
                • Adaptation decisions are validated locally via PolicyValidator""".trimMargin(),
            style = MaterialTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // How long it is stored section
Text(
                text = getString(R.string.privacy_how_long_stored),
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

        Text(
            text = """• Behavioral events are stored until manually cleared by you
                • Adaptation state and preferences persist across app launches
                • You can clear all behavioral history at any time from this screen
                • Clearing history resets adaptive learning progress
                • Individual event retention is governed by the app's storage lifecycle""".trimMargin(),
            style = MaterialTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // How to delete it section
Text(
                text = getString(R.string.privacy_how_to_delete),
                style = MaterialTypography.titleSmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

        Text(
            text = """• Select "Clear behavioral history" to delete all stored interaction events
                • Cleared data cannot be recovered within the app
                • Adaptive behavior will restart from a cold start after clearing
                • You can also disable adaptive UI to stop further data collection
                • No data is ever transmitted externally, so local clearing is fully effective""".trimMargin(),
            style = MaterialTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Adaptive UI opt-in toggle
        OutlinedButton(
            onClick = { onAdaptiveOptInChanged(!adaptiveOpt) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            HStack(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
Text(
                        text = if (adaptiveOpt) getString(R.string.adaptive_opt_in_enabled)
                            else getString(R.string.adaptive_opt_in_disabled),
                        style = MaterialTypography.bodyMedium
                    )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (adaptiveOpt) {
                        androidx.compose.material.icons.FontAwesome5.Sun
                    } else {
                        androidx.compose.material.icons.FontAwesome5.Moon
                    },
                    contentDescription = if (adaptiveOpt) "Adaptive UI enabled" else "Adaptive UI disabled"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear behavioral history button
        OutlinedButton(
            onClick = { clearHistory() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            HStack(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Text("clear_history", style = MaterialTypography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Trash,
                    contentDescription = "Clear behavioral history"
                )
            }
        }
    }
}