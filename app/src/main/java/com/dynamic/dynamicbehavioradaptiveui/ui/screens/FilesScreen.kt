package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun FilesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Files", style = MaterialTypography.titleLarge) },
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
                text = "Files",
                style = MaterialTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                HStack(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.Folder,
                        contentDescription = "Files"
                    )
                    Text("No files yet", style = MaterialTypography.bodyMedium)
                }
            }
        }
    }
}