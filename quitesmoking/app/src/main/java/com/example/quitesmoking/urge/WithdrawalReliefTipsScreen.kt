package com.example.quitesmoking.urge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalReliefTipsScreen(navController: NavController) {
    
    val withdrawalSymptoms = listOf(
        WithdrawalSymptom(
            title = "Nicotine Cravings",
            description = "Intense desire to smoke, usually peaks in first 3 days",
            duration = "2-4 weeks",
            tips = listOf(
                "Use nicotine replacement therapy (patches, gum, lozenges)",
                "Stay hydrated and drink plenty of water",
                "Practice deep breathing exercises",
                "Keep your hands busy with stress balls or fidget toys"
            )
        ),
        WithdrawalSymptom(
            title = "Irritability & Mood Swings",
            description = "Feeling easily annoyed, anxious, or depressed",
            duration = "2-4 weeks",
            tips = listOf(
                "Practice mindfulness and meditation",
                "Exercise regularly to boost mood",
                "Get adequate sleep (7-9 hours)",
                "Talk to supportive friends or family"
            )
        ),
        WithdrawalSymptom(
            title = "Difficulty Concentrating",
            description = "Trouble focusing on tasks or remembering things",
            duration = "1-2 weeks",
            tips = listOf(
                "Break tasks into smaller, manageable chunks",
                "Take regular breaks every 30 minutes",
                "Use a planner or to-do list",
                "Avoid multitasking during this period"
            )
        ),
        WithdrawalSymptom(
            title = "Sleep Problems",
            description = "Difficulty falling asleep or staying asleep",
            duration = "1-4 weeks",
            tips = listOf(
                "Establish a regular sleep schedule",
                "Avoid caffeine after 2 PM",
                "Create a relaxing bedtime routine",
                "Keep your bedroom cool and dark"
            )
        ),
        WithdrawalSymptom(
            title = "Increased Appetite",
            description = "Stronger hunger and potential weight gain",
            duration = "Several weeks",
            tips = listOf(
                "Choose healthy snacks (fruits, vegetables, nuts)",
                "Stay hydrated to avoid confusing thirst with hunger",
                "Eat regular, balanced meals",
                "Exercise to manage weight and stress"
            )
        ),
        WithdrawalSymptom(
            title = "Constipation",
            description = "Slower digestion and bowel movements",
            duration = "1-2 weeks",
            tips = listOf(
                "Increase fiber intake (fruits, vegetables, whole grains)",
                "Drink plenty of water",
                "Exercise regularly",
                "Consider over-the-counter fiber supplements"
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdrawal Relief Tips") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))
                    )
                )
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Understanding Withdrawal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nicotine withdrawal symptoms are temporary and manageable. " +
                            "Most symptoms peak within the first 3 days and gradually improve.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // General tips section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF388E3C)
                            )
                            Text(
                                "General Coping Strategies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Remember: This is temporary and you're getting healthier every day",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Celebrate small victories and milestones",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Use the 4 D's: Delay, Deep breathe, Drink water, Do something else",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Keep a journal to track your progress and feelings",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Individual symptom cards
//            items(withdrawalSymptoms) { symptom ->
//                WithdrawalSymptomCard(symptom = symptom)
//            }
            // Inside LazyColumn { ... }
            items(
                items = withdrawalSymptoms,
                key = { it.title }   // stable key per item
            ) { symptom ->
                ExpandableSymptomCard(symptom = symptom)
            }


            // Emergency section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Text(
                                "When to Seek Help",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Contact your healthcare provider if you experience:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Severe depression or thoughts of self-harm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Chest pain or heart palpitations",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Persistent insomnia affecting daily life",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "• Symptoms that don't improve after 4 weeks",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun WithdrawalSymptomCard(symptom: WithdrawalSymptom) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symptom.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF757575)
                    )
                    Text(
                        text = symptom.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = symptom.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Relief Tips:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            symptom.tips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF388E3C)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSymptomCard(
    symptom: WithdrawalSymptom,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(symptom.title) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(), // smooth height animation
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surface else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (expanded) 6.dp else 3.dp),
        onClick = { expanded = !expanded } // tap anywhere toggles
    ) {
        Column {
            // Header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading accent
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1976D2), Color(0xFF64B5F6))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = symptom.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = symptom.duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Chevron rotates on expand
                Icon(
                    imageVector = Icons.Default.ArrowBack, // using AutoMirrored? If so, swap to appropriate arrow
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = if (expanded) 90f else -90f }, // rotate to chevron-down/up feel
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Divider between header and details
            if (expanded) {
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            }

            // Body (revealed on expand)
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = symptom.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Relief Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )

                    Spacer(Modifier.height(8.dp))

                    symptom.tips.forEach { tip ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•  ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF388E3C)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    // Optional: “Quick actions” row
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip("Breathe")
                        SuggestionChip("Hydrate")
                        SuggestionChip("Move")
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String) {
    AssistChip(
        onClick = { /* hook quick action if needed */ },
        label = { Text(text) },
        shape = RoundedCornerShape(12.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            labelColor = MaterialTheme.colorScheme.primary
        )
    )
}

data class WithdrawalSymptom(
    val title: String,
    val description: String,
    val duration: String,
    val tips: List<String>
)
