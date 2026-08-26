package com.dynamic.dynamicbehavioradaptiveui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dynamic.dynamicbehavioradaptiveui.ui.screens.*
import com.dynamic.dynamicbehavioradaptiveui.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DynamicBehaviorDrivenAdaptiveUiTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    composable("home") { HomeScreen(navController) }
                    composable("tasks") { TasksScreen() }
                    composable("calendar") { CalendarScreen() }
                    composable("notes") { NotesScreen() }
                    composable("files") { FilesScreen() }
                    composable("settings") { SettingsScreen() }
                    composable("privacy") { PrivacyScreen(onBack = { navController.navigate("settings") }) }
                }

                DemoOverlay()
            }
        }
    }
}