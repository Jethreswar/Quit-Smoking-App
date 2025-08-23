@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.quitesmoking.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LeaderboardEntry(
    val user: String = "",
    val days: Int = 0,
    val timestamp: Date = Date()
)

@SuppressLint("NewApi")
@Composable
fun WeeklyGoalBuilderScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val firstCheckin = remember { Calendar.getInstance().apply { add(Calendar.DATE, -30) } }
    val currentStart = remember { Calendar.getInstance().apply { add(Calendar.DATE, -2) } }
    val currentEnd = remember { Calendar.getInstance() }
    val format = SimpleDateFormat("MMM d", Locale.getDefault())
    var selectedRange by remember { mutableStateOf("${format.format(currentStart.time)} - ${format.format(currentEnd.time)}") }

    val dateOptions = remember {
        (0..30).map { offset ->
            val start = Calendar.getInstance().apply { time = firstCheckin.time; add(Calendar.DATE, offset) }
            val end = Calendar.getInstance().apply { time = start.time; add(Calendar.DATE, 2) }
            "${format.format(start.time)} - ${format.format(end.time)}"
        }
    }

    var dateMenuExpanded by remember { mutableStateOf(false) }

    var userName     by remember { mutableStateOf("Anonymous") }
    var editingName  by remember { mutableStateOf(false) }
    var userDays     by remember { mutableStateOf((1..10).random()) }
    var leaderboard  by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    // 🔥 read  Firestore  ranking data
    LaunchedEffect(selectedRange) {
        db.collection("leaderboard")
            .orderBy("days", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
//        db.collection("leaderboard")
//            .orderBy("days", com.google.firebase.firestore.Query.Direction.DESCENDING)
//            .get()
            .addOnSuccessListener { result ->
                val entries = result.mapNotNull { doc ->
                    val user = doc.getString("user") ?: return@mapNotNull null
                    val days = doc.getLong("days")?.toInt() ?: return@mapNotNull null
                    val timestamp = doc.getDate("timestamp") ?: Date()
                    LeaderboardEntry(user, days, timestamp)
                }
                leaderboard = entries
            }
            .addOnFailureListener {
                scope.launch {
                    snackbarHostState.showSnackbar("❌ Failed to load leaderboard.")
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quit EC") },
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
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = 0) {
                Tab(selected = true, onClick = {}) { Text("Leaderboard") }
                Tab(selected = false, onClick = {}) { Text("Community") }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = selectedRange, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { dateMenuExpanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = dateMenuExpanded,
                    onDismissRequest = { dateMenuExpanded = false }
                ) {
                    dateOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedRange = it
                                dateMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("You appear as ", style = MaterialTheme.typography.bodySmall)
                    if (editingName) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            singleLine = true,
                            modifier = Modifier.width(140.dp)
                        )
                    } else {
                        Text(userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = { editingName = !editingName }) {
                    Text(if (editingName) "Done" else "Edit")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Leaderboard for $selectedRange", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    scope.launch {
//                        db.collection("leaderboard").add(
//                            mapOf("user" to userName, "days" to userDays, "timestamp" to Date())
//                        )
                        snackbarHostState.showSnackbar("✅ Progress shared!")
                        delay(2000)
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Share Your Progress")
            }

            Divider()

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(leaderboard) { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#${index + 1} ${entry.user}")
                        Text("${entry.days} days", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
