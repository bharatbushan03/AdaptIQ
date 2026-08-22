package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels.NotesViewModel
import com.dynamic.dynamicbehavioradaptiveui.models.Note
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    val vm = viewModel()
    val notes by vm.notes.collectAsList()

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
            // Header
            Text(
                text = "My Notes",
                style = MaterialTypography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            if (notes.isEmpty()) {
                OutlinedButton(
                    onClick = { vm.addNote("New Note", "Enter note content") },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    HStack(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                            contentDescription = "Add Note"
                        )
                        Text("Add Note", style = MaterialTypography.bodyMedium)
                    }
                }
            } else {
                // Notes list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    notes.forEach { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTypography.titleMedium,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTypography.bodySmall,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 3
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { vm.deleteNote(note.id) },
                                        modifier = Modifier.width(60.dp)
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }

                // Add note FAB
                FloatingActionButton(onClick = { vm.addNote("New Note", "Enter content") }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                        contentDescription = "Add Note"
                    )
                }.align(Alignment.BottomEnd)
            }
        }
    }
}