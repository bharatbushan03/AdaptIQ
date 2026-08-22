package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.dynamic.dynamicbehavioradaptiveui.adaptation.Adaptation
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptationType
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptiveUIState
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun ContextualShortcutBar(
    state: AdaptiveUIState,
    onShortcutSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val shortcuts = remember(state) {
        val currentIntent = state.behaviorState.currentIntent
        if (currentIntent == "repeated_workflow") {
            val mostFrequent = state.behaviorState.proficiencyLevel
            when (mostFrequent) {
                ProficiencyLevel.ADVANCED -> listOf(
                    "new_event",
                    "new_task"
                )
                ProficiencyLevel.INTERMEDIATE -> listOf(
                    "quick_add"
                )
                ProficiencyLevel.BEGINNER -> listOf(
                    "create_first"
                )
            }
        } else emptyList()
    }

    if (shortcuts.isEmpty()) return Unit

    OutlinedButton(
        onClick = { /* handled elsewhere */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        enabled = enabled,
        style = OutlinedButtonStyle.outlinedCopy()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            shortcuts.forEachIndexed { index, shortcut ->
                val shortcutContent = when (shortcut) {
                    "new_event" -> {
                        val calendarEvents = remember { 2 }
                        "$calendarEvents Event"
                    }
                    "new_task" -> {
                        val tasks = remember { 3 }
                        "New Task($tasks)"
                    }
                    "quick_add" -> "Quick Add"
                    "create_first" -> "Create First"
                    else -> shortcut
                }

                OutlinedButton(
                    onClick = { onShortcutSelected(shortcut) },
                    modifier = Modifier.weight(1f),
                    style = OutlinedButtonStyle.outlinedButton
                ) {
                    Text(
                        text = shortcutContent,
                        style = MaterialCaption,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveActionGroup(
    state: AdaptiveUIState,
    actions: List<AdaptiveAction>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val visibleActions = remember(state) {
        filterActions(actions, state.behaviorState)
    }

    if (visibleActions.isEmpty()) return Unit

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(AdapteriveActionGroupConstants.groupHeight),
        verticalArrangement = Arrangement.spacedBy(
            AdapteriveActionGroupConstants.spacing
        ),
        horizontalAlignment = AdapteriveActionGroupConstants.alignment
    ) {
        visibleActions.forEach { action ->
            AdaptiveActionButton(
                action = action,
                onSelect = { /* handled */ },
                enabled = enabled && action.isAvailableFor(state.behaviorState)
            )
        }
    }
}

@Composable
fun AdaptiveActionButton(
    action: AdaptiveAction,
    onSelect: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = if (enabled) onSelect else {},
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        enabled = enabled,
        style = OutlinedButtonStyle.outlinedButton
    ) {
        Box(
            modifier = Modifier
                .size(AdapteriveActionButtonConstants.size)
                .then(ContentPadding)
        ) {
            Icon(
                imageVector = resolveIcon(action.iconName),
                contentDescription = action.contentDescription,
                tint = if (enabled) Color.Unspecified else Color.Gray
            )
            Text(
                text = action.label,
                style = MaterialCaption,
                textAlign = TextAlign.Center,
                opacity = if (enabled) 1f else 0.5f
            )
        }
    }
}

@Composable
fun ContextualGuidanceCard(
    state: AdaptiveUIState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val friction = state.behaviorState.interactionFriction
    val proficiency = state.behaviorState.proficiencyLevel
    val intent = state.behaviorState.currentIntent

    val showCard = remember(state) {
        friction == InteractionFriction.HIGH &&
        (intent == "in_progress_workflow" || intent == "repeated_workflow")
    }

    if (!showCard) return Unit

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardElevationDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardColors(
            defaultColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AdapteriveGuidanceConstants.verticalSpacing),
            horizontalAlignment = AdapteriveGuidanceConstants.horizontalAlignment
        ) {
            Text(
                text = guidanceTitle(friction, proficiency),
                style = MaterialTitleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = guidanceMessage(intent, proficiency),
                style = MaterialBodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .wrapContentSize(Alignment.End)
                    .padding(AdapteriveGuidanceConstants.buttonPadding),
                style = OutlinedButtonStyle.outlinedButton
            ) {
                Text(
                    text = "Got it",
                    style = MaterialCaption
                )
            }
        }
    }
}

@Composable
fun FeatureRecommendation(
    state: AdaptiveUIState,
    availableFeatures: List<FeatureRecommendationFeature>,
    onFeatureSelected: (FeatureRecommendationFeature) -> Unit
) {
    val proficiency = state.behaviorState.proficiencyLevel
    val friction = state.behaviorState.interactionFriction
    val familiarity = state.behaviorState.workflowFamiliarity

    val recommendedFeatures = remember(state) {
        filterAndRecommendFeatures(availableFeatures, proficiency, friction, familiarity)
    }

    if (recommendedFeatures.isEmpty()) return Unit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardElevationDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardColors(
            defaultColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Recommended for you",
                style = MaterialTitleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            ForEach(recommendedFeatures) { feature ->
                FeatureRecommendationItem(
                    feature = feature,
                    onSelect = { onFeatureSelected(it) }
                )
            }
        }
    }
}

@Composable
fun InformationDensityController(
    state: AdaptiveUIState,
    onDensityChanged: (Int) -> Unit,
    currentDensity: Int
) {
    val proficiency = state.behaviorState.proficiencyLevel
    val optimalDensity = remember(state) {
        calculateOptimalDensity(proficiency)
    }

    if (optimalDensity == currentDensity) return Unit

    val animateDensity = remember { mutableStateOf(currentDensity) }

    AnimatedVisibility(
        enter = fadeIn(),
        exit = fadeOut(),
        anchor = Alignment.TopStart
    ) {
        TextButton(
            onClick = {
                onDensityChanged(optimalDensity)
                animateDensity.current = optimalDensity
            },
            modifier = Modifier
                .padding(16.dp)
                .then(ContentPadding)
        ) {
            Text(
                text = densityLabel(optimalDensity),
                style = MaterialBodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AdaptiveActionIcon(
    iconName: String,
    contentDescription: String?,
    tint: Color = Color.Unspecified
) {
    val icon = when (iconName) {
        "calendar-event" -> androidx.compose.material.icons.FontAwesome5.CalendarEvent
        "calendar-new" -> androidx.compose.material.icons.FontAwesome5.CalendarAlt
        "task-new" -> androidx.compose.material.icons.FontAwesome5.Taskboard
        "task-quick" -> androidx.compose.material.icons.FontAwesome5.Regular.Taskboard
        "create" -> androidx.compose.material.icons.FontAwesome5.Regular.Plus
        else -> androidx.compose.material.icons.FontAwesome5.Regular.QuestionCircle
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint
    )
}

@Composable
fun FeatureRecommendationItem(
    feature: FeatureRecommendationFeature,
    onSelect: () -> Unit
) {
    OutlinedButton(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        style = OutlinedButtonStyle.outlinedButton
    ) {
        Box(
            modifier = Modifier
                .height(FeatureRecommendationItemConstants.itemHeight)
                .then(FeatureRecommendationItemContentPadding)
        ) {
            AdaptiveActionIcon(
                iconName = feature.iconName,
                contentDescription = feature.contentDescription
            )
            Text(
                text = feature.label,
                style = MaterialCaption,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeatureRecommendationIcon(
    painter: Painter?,
    contentDescription: String?
) {
    Icon(
        imageVector = painter != null
            ? androidx.compose.ui.platform.paint(painter)
            : androidx.compose.material.icons.FontAwesome5.Regular.QuestionCircle,
        contentDescription = contentDescription
    )
}

private fun resolveIcon(iconName: String): ImageVector {
    return when (iconName) {
        "calendar-event" -> androidx.compose.material.icons.FontAwesome5.CalendarEvent
        "calendar-new" -> androidx.compose.material.icons.FontAwesome5.CalendarAlt
        "task-new" -> androidx.compose.material.icons.FontAwesome5.Taskboard
        "task-quick" -> androidx.compose.material.icons.FontAwesome5.Regular.Taskboard
        "create" -> androidx.compose.material.icons.FontAwesome5.Regular.Plus
        else -> androidx.compose.material.icons.FontAwesome5.Regular.QuestionCircle
    }
}

private fun filterActions(
    actions: List<AdaptiveAction>,
    behaviorState: BehaviorState
): List<AdaptiveAction> {
    return actions.filter { action ->
        action.isAvailableFor(behaviorState)
    }
}

private fun filterAndRecommendFeatures(
    features: List<FeatureRecommendationFeature>,
    proficiency: ProficiencyLevel,
    friction: InteractionFriction,
    familiarity: WorkflowFamiliarity
): List<FeatureRecommendationFeature> {
    return when (proficiency) {
        ProficiencyLevel.BEGINNER -> features.take(2)
        ProficiencyLevel.INTERMEDIATE -> features.filter { it.intermediateSuitable }
        ProficiencyLevel.ADVANCED -> features.filter { it.advancedSuitable }
    }.take(3)
}

fun calculateOptimalDensity(proficiency: ProficiencyLevel): Int {
    return when (proficiency) {
        ProficiencyLevel.BEGINNER -> 160
        ProficiencyLevel.INTERMEDIATE -> 120
        ProficiencyLevel.ADVANCED -> 80
    }
}

fun densityLabel(density: Int): String {
    when (density) {
        160 -> "Comfortable"
        120 -> "Compact"
        80 -> "Efficient"
        else -> "Medium"
    }
}

fun guidanceTitle(friction: InteractionFriction, proficiency: ProficiencyLevel): String {
    return when (friction) {
        InteractionFriction.HIGH -> "Take your time - you're doing great!"
        else -> "Here's what's next"
    }
}

fun guidanceMessage(intent: String, proficiency: ProficiencyLevel): String {
    return when (proficiency) {
        ProficiencyLevel.BEGINNER -> "Keep exploring! Each tap helps you learn."
        ProficiencyLevel.INTERMEDIATE -> "You're getting the hang of this."
        ProficiencyLevel.ADVANCED -> "Ready for the next step?"
    }
}

data class AdaptiveAction(
    val label: String,
    val iconName: String,
    val contentDescription: String,
    val isAvailableFor: (BehaviorState) -> Boolean = { _ -> true }
)

data class FeatureRecommendationFeature(
    val label: String,
    val iconName: String,
    val contentDescription: String,
    val intermediateSuitable: Boolean = true,
    val advancedSuitable: Boolean = true
)

object AdapteriveActionGroupConstants {
    val groupHeight = 80.dp
    val spacing = 8.dp
    val alignment = Alignment.CenterStart
}

object AdaptiveActionButtonConstants {
    val size = 40.dp
}

object AdapteriveGuidanceConstants {
    val verticalSpacing = 12.dp
    val horizontalAlignment = Alignment.Center
    val buttonPadding = 8.dp
}

object AdaptiveGuidanceCardConstants {
    val verticalSpacing = 12.dp
    val horizontalAlignment = Alignment.CenterStart
    val buttonPadding = 8.dp
}

object FeatureRecommendationItemConstants {
    val itemHeight = 64.dp
}

object ContentPadding {
    val value = Modifier.padding(8.dp)
}

object FeatureRecommendationItemContentPadding {
    val value = Modifier.padding(4.dp)
}