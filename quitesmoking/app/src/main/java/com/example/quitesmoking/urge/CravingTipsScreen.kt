package com.example.quitesmoking.urge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CravingTipsScreen(navController: NavController) {
    val encouragements = listOf(
        "You're doing great!",
        "Stay strong, you've got this!",
        "One step at a time!",
        "Keep going, you're amazing!",
        "You are stronger than your cravings!"
    )

    val cardCount = 6
    val cardStates = remember {
        List(cardCount) { mutableStateOf("Tap to reveal") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mild Craving Tips") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFB2EBF2), Color(0xFFB2DFDB))
                    )
                )
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tap each card to reveal a tip:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cardStates) { state ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                state.value = encouragements.random()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB2DFDB)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.value,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
