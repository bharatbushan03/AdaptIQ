package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dynamic.dynamicbehavioradaptiveui.ui.viewmodels.CalendarViewModel
import com.dynamic.dynamicbehavioradaptiveui.models.CalendarEvent
import com.dynamic.dynamicbehavioradaptiveui.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val vm = viewModel()
    val events by vm.events.collectAsList()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showEventDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    // Day column header
    val dayHeaders = IndexedSequence(LocalDate.now().withDayOfMonth(1), LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1))
        .rangeToIndex(31) { it -> it.getDayOfWeek(TextLocale = Locale.getDefault())?.name ?? it.dayOfMonth.toString() }

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
                    OutlinedButton(onClick = { showEventDialog = true }) {
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
            // Calendar header with month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { /* previous month */ }) {
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
                OutlinedButton(onClick = { /* next month */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.FontAwesome5.ChevronRight,
                        contentDescription = "Next month"
                    )
                }
            }

            // Day grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Day headers
                dayHeaders.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTypography.bodyMedium,
                        modifier = Modifier.width(36.dp).height(36.dp),
                        horizontalAlignment = Alignment.Center
                    )
                }
            }

            // Event list
            if (events.isEmpty()) {
                Text(
                    text = "No events for ${selectedDate.month.name.capitalize()}",
                    style = MaterialTypography.bodyMedium,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                events.forEach { event ->
                    if (event.startTime != 0L) {
                        try {
                            val eventDate = LocalDate.ofEpochDay(event.startTime / 86400000)
                            if (eventDate == selectedDate) {
                                OutlinedButton(
                                    onClick = { showEventDialog = true; eventTitle = event.title; eventDescription = event.description },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                ) {
                                    HStack(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        contentPadding = 12.dp
                                    ) {
                                        Text(
                                            text = "${event.title} — ${eventDate.day} ${eventDate.month.name.capitalize()}",
                                            style = MaterialTypography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (event.allDay) {
                                            ImageVector(
                                                imageVector = androidx.compose.material.icons.FontAwesome5.CalendarDay,
                                                contentDescription = "All day event"
                                            )
                                        } else {
                                            Text(
                                                text = "${event.endTime - event.startTime} min",
                                                style = MaterialTypography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Skip malformed events
                        }
                    }
                }
            }

            // Add event FAB at bottom
            FloatingActionButton(onClick = { showEventDialog = true }) {
                Icon(
                    imageVector = androidx.compose.material.icons.FontAwesome5.Plus,
                    contentDescription = "Add Event"
                )
            }.align(Alignment.BottomEnd)
        }
    }
}