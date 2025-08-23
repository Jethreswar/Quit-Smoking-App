package com.example.quitesmoking.chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun LeaderboardScreen() {
    val db = FirebaseFirestore.getInstance()
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("leaderboards").document("allTime").get().await()
            val rawList = snapshot.get("data")
            if (rawList !is List<*>) {
                errorMessage = "Data format is invalid."
                return@LaunchedEffect
            }

            leaderboard = rawList.mapNotNull {
                val map = it as? Map<*, *> ?: return@mapNotNull null
                val name = map["nickname"] as? String ?: return@mapNotNull null
                val count = (map["smokeFreeDays"] as? Long)?.toInt() ?: 0
                LeaderboardEntry(name, count)
            }
        } catch (e: Exception) {
            Log.e("LeaderboardScreen", "Error fetching leaderboard", e)
            errorMessage = "⚠️ Failed to load data. Please check your Internet connection."
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboard - All Time", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            leaderboard.isEmpty() -> Text("No leaderboard data available.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(leaderboard) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.nickname)
                            Text("${entry.smokeFreeDays} days")
                        }
                    }
                }
            }
        }
    }
}

data class LeaderboardEntry(
    val nickname: String,
    val smokeFreeDays: Int
)
