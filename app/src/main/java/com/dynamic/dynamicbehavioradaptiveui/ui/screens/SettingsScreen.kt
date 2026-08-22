package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun SettingsScreen() {
    var theme by remember { mutableStateOf("Light") }

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

            // Adaptation mode
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text("Adaptive UI", style = MaterialTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.ArrowRight,
                        contentDescription = "Expand"
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
        }
    }
}