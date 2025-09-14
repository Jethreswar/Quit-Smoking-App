package com.example.quitesmoking.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quitesmoking.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Chat", "Leaderboard")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community") },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Navigate to Home tab since this is part of bottom navigation
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Header
            Text(
                text = "💬 You're not alone! Support each other with no judgement!",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Be Kind. Encourage. Don't Judge.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> ChatScreen(navController)
                1 -> LeaderboardScreen() // Placeholder, implement separately
            }
        }
    }
}