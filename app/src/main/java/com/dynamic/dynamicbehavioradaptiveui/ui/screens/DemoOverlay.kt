package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawable.shapes.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.dynamic.dynamicbehavioradaptiveui.adaptation.DemoMode
import com.dynamic.dynamicbehavioradaptiveui.adaptation.DemoMode.DemoScenario
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.ProficiencyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionFriction

@Composable
fun DemoOverlay() {
    val isDemo = DemoMode.isDemoMode()
    if (!isDemo) return

    val scenario = DemoMode.getCurrentScenario()
    val overlay = DemoMode.getOverlayState()
    val events = DemoMode.getEventsList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0x881a1a2e),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            _buildScenarioHeader(scenario)
            _buildBehaviorSummary(overlay)
            _buildLLMCard(overlay)
            _buildAdaptationCard(overlay)
            _buildOutcomeCard(overlay)
            _buildEventLog(events)
        }
    }
}

private fun _buildScenarioHeader(scenario: DemoScenario?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (scenario) {
                    is DemoScenario.NewUser -> {
                        Text(
                            text = "SCENARIO 1: NEW USER",
                            style = MaterialTypography.titleSmall,
                            color = Color(0xff1976d2)
                        )
                    }
                    is DemoScenario.RepeatedUser -> {
                        Text(
                            text = "SCENARIO 2: REPEATED USER",
                            style = MaterialTypography.titleSmall,
                            color = Color(0xff673ab7)
                        )
                    }
                    is DemoScenario.FrictionUser -> {
                        Text(
                            text = "SCENARIO 3: FRICTION DETECTION",
                            style = MaterialTypography.titleSmall,
                            color = Color(0xffd84315)
                        )
                    }
                    else -> Text("Scenario: None")
                }
            }

            Text(
                text = scenario?.description ?: "",
                style = MaterialTypography.bodySmall,
                color = Color(0xff666666),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun _buildBehaviorSummary(overlay: DemoMode.OverlayState) {
    val profColor = when (overlay.proficiency) {
        ProficiencyLevel.BEGINNER -> Color(0xfff44336)
        ProficiencyLevel.INTERMEDIATE -> Color(0xfffb8c00)
        ProficiencyLevel.ADVANCED -> Color(0xff4caf50)
        else -> Color(0xff9e9e9e)
    }
    val friColor = when (overlay.friction) {
        InteractionFriction.LOW -> Color(0xff4caf50)
        InteractionFriction.MEDIUM -> Color(0xfffb8c00)
        InteractionFriction.HIGH -> Color(0xfff44336)
        else -> Color(0xff9e9e9e)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            _buildInfoRow("Proficiency:", overlay.proficiency.toString(), profColor)
            _buildInfoRow("Intent:", overlay.intent, Color(0xff1976d2))
            _buildInfoRow("Friction:", overlay.friction.toString(), friColor)
        }
    }
}

private fun _buildInfoRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTypography.bodyMedium, color = Color(0xff333333))
        Spacer(Modifier.width(8.dp))
        Text(text = value, style = MaterialTypography.bodyMedium, color = color)
    }
}

private fun _buildLLMCard(overlay: DemoMode.OverlayState) {
    val confColor = when (overlay.confidence) {
        confidence -> confidence >= 0.75 -> Color(0xff4caf50)
        confidence -> confidence >= 0.5 -> Color(0xfffb8c00)
        else -> Color(0xfff44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LLM Recommendation:",
                    style = MaterialTypography.bodyMedium,
                    color = Color(0xff333333)
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = overlay.llmRecommendation ?: "No recommendation yet",
                style = MaterialTypography.bodySmall,
                color = Color(0xff212121),
                maxLines = 3,
                overflow = TextOverflow.Elipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Confidence: ${String.format("%.2f", overlay.confidence)}",
                    style = MaterialTypography.bodySmall,
                    color = confColor
                )
            }
        }
    }
}

private fun _buildAdaptationCard(overlay: DemoMode.OverlayState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adaptation Applied:",
                    style = MaterialTypography.bodyMedium,
                    color = Color(0xff333333)
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = overlay.adaptationApplied ?: "No adaptation applied",
                style = MaterialTypography.bodySmall,
                color = Color(0xff212121),
                maxLines = 3,
                overflow = TextOverflow.Elipsis
            )
        }
    }
}

private fun _buildOutcomeCard(overlay: DemoMode.OverlayState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Measured Outcome:",
                    style = MaterialTypography.bodyMedium,
                    color = Color(0xff333333)
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = overlay.measuredOutcome ?: "No outcome measured yet",
                style = MaterialTypography.bodySmall,
                color = Color(0xff212121),
                maxLines = 3,
                overflow = TextOverflow.Elipsis
            )
        }
    }
}

private fun _buildEventLog(events: List<com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent>) {
    var expanded by remember { false }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        colors = CardColors(defaultColor = Color(0xffe8e8f0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Events Collected: ${events.size}",
                    style = MaterialTypography.bodyMedium,
                    color = Color(0xff333333)
                )
                Button(
                    onClick = { expanded = !expanded }
                ) {
                    Text(
                        expanded ? "Collapse" : "Expand",
                        style = MaterialTypography.bodySmall,
                        color = Color(0xff1976d2)
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    events.forEach { event ->
                        Text(
                            text = "${event.screen}.${event.action}→${event.target} (success: ${event.success})",
                            style = MaterialTypography.caption,
                            color = Color(0xff424242),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}