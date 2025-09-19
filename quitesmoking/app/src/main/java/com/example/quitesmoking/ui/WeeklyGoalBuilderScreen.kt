@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.quitesmoking.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quitesmoking.navigation.goHomeInTabs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.ZoneId

data class LeaderboardEntry(
    val user: String = "",
    val days: Int = 0,
    val timestamp: Date = Date()
)

data class CommunityMessage(
    val user: String = "",
    val message: String = "",
    val timestamp: Date = Date()
)

// Function to filter leaderboard entries by selected date range
private fun filterEntriesByDateRange(entries: List<LeaderboardEntry>, range: String): List<LeaderboardEntry> {
    return try {
        // If range is empty or null, return all entries
        if (range.isBlank()) return entries
        
        // Parse the selected range (e.g., "Aug 22 - Aug 24")
        val dateParts = range.split(" - ")
        if (dateParts.size != 2) return entries
        
        val startDateStr = dateParts[0].trim()
        val endDateStr = dateParts[1].trim()
        
        // If either date part is empty, return all entries
        if (startDateStr.isBlank() || endDateStr.isBlank()) return entries
        
        // Parse dates using the same format as dateOptions
        val format = SimpleDateFormat("MMM d", Locale.getDefault())
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val startDate = format.parse("$startDateStr $currentYear")
        val endDate = format.parse("$endDateStr $currentYear")
        
        if (startDate == null || endDate == null) return entries
        
        // Create calendar instances for comparison
        val startCal = Calendar.getInstance().apply { time = startDate }
        val endCal = Calendar.getInstance().apply { time = endDate }
        
        // Set time to start and end of day for proper comparison
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        
        // Filter entries that fall within the date range
        entries.filter { entry ->
            val entryCal = Calendar.getInstance().apply { time = entry.timestamp }
            entryCal.after(startCal) && entryCal.before(endCal) || 
            entryCal.timeInMillis == startCal.timeInMillis || 
            entryCal.timeInMillis == endCal.timeInMillis
        }
    } catch (e: Exception) {
        // If parsing fails, return all entries
        entries
    }
}

@SuppressLint("NewApi")
@Composable
fun WeeklyGoalBuilderScreen(navController: NavController, bottomNav: NavController) {
    androidx.activity.compose.BackHandler { bottomNav.goHomeInTabs() }
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
        listOf("All Time") + (0..30).map { offset ->
            val start = Calendar.getInstance().apply { time = firstCheckin.time; add(Calendar.DATE, offset) }
            val end = Calendar.getInstance().apply { time = start.time; add(Calendar.DATE, 2) }
            "${format.format(start.time)} - ${format.format(end.time)}"
        }
    }

    var dateMenuExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    var userName     by remember { mutableStateOf("Anonymous") }
    var editingName  by remember { mutableStateOf(false) }
    var userDays     by remember { mutableIntStateOf(0) }
    var leaderboard  by remember { mutableStateOf(listOf<LeaderboardEntry>()) }
    var allLeaderboardEntries by remember { mutableStateOf(listOf<LeaderboardEntry>()) }
    var isLoading    by remember { mutableStateOf(true) }
    
    // Community tab variables
    var communityMessages by remember { mutableStateOf(listOf<CommunityMessage>()) }
    var newMessage by remember { mutableStateOf("") }

    // Calculate user's actual smoke-free days
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                // Get user's display name
                val user = FirebaseAuth.getInstance().currentUser
                userName = user?.displayName ?: user?.email?.split("@")?.first() ?: "Anonymous"
                
                // Calculate actual smoke-free days from dailyCheckResponses
                val checkinSnapshot = db.collection("users")
                    .document(userId)
                    .collection("dailyCheckResponses")
                    .get()
                    .await()
                
                val sorted = checkinSnapshot.documents
                    .mapNotNull {
                        val ts = it.getTimestamp("timestamp")?.toDate()?.toInstant()
                        val date = ts?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        val answer = it.getString("answer")
                        if (date != null && (answer == "No" || answer.equals("no", ignoreCase = true) || answer == "否")) date else null
                    }
                    .distinct() // Remove duplicates for same day
                    .sortedDescending()

                userDays = sorted.size
                
            } catch (e: Exception) {
                userDays = 0
            } finally {
                isLoading = false
            }
        }
    }

    // 🔥 read  Firestore  ranking data with real-time updates
    LaunchedEffect(Unit) {
        db.collection("leaderboard")
            .orderBy("days", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("❌ Failed to load leaderboard.")
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        val user = doc.getString("user") ?: return@mapNotNull null
                        val days = doc.getLong("days")?.toInt() ?: return@mapNotNull null
                        val timestamp = doc.getDate("timestamp") ?: Date()
                        LeaderboardEntry(user, days, timestamp)
                    }
                    
                    // Store all entries
                    allLeaderboardEntries = entries
                    
                    // Apply filtering based on selected range
                    if (selectedRange == "All Time") {
                        leaderboard = entries
                    } else {
                        val filteredEntries = filterEntriesByDateRange(entries, selectedRange)
                        leaderboard = filteredEntries
                    }
                }
            }
    }

    // Apply filtering when selectedRange changes
    LaunchedEffect(selectedRange, allLeaderboardEntries) {
        if (allLeaderboardEntries.isNotEmpty()) {
            if (selectedRange == "All Time") {
                leaderboard = allLeaderboardEntries
            } else {
                val filteredEntries = filterEntriesByDateRange(allLeaderboardEntries, selectedRange)
                leaderboard = filteredEntries
            }
        }
    }

    // Load community messages
    LaunchedEffect(Unit) {
        db.collection("community_messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("❌ Failed to load messages.")
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        val user = doc.getString("user") ?: return@mapNotNull null
                        val message = doc.getString("message") ?: return@mapNotNull null
                        val timestamp = doc.getDate("timestamp") ?: Date()
                        CommunityMessage(user, message, timestamp)
                    }
                    communityMessages = messages
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quit EC") },
                navigationIcon = {
                    IconButton(onClick = { bottomNav.goHomeInTabs() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0, 
                    onClick = { selectedTab = 0 }
                ) { 
                    Text("Leaderboard") 
                }
                Tab(
                    selected = selectedTab == 1, 
                    onClick = { selectedTab = 1 }
                ) { 
                    Text("Community") 
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Leaderboard Tab Content
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

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show current user's progress
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Your Progress", style = MaterialTheme.typography.titleMedium)
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                Text("$userDays smoke-free days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                if (userDays == 0) {
                                    Text("Start your journey today!", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    Text("Keep going!", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Leaderboard for $selectedRange", style = MaterialTheme.typography.titleMedium)
                    
                    // Show filtered results count
                    if (leaderboard.isNotEmpty()) {
                        Text(
                            "Showing ${leaderboard.size} entries for selected period",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "No entries found for selected period",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // Check if user already exists in leaderboard
                                    val existingQuery = db.collection("leaderboard")
                                        .whereEqualTo("user", userName)
                                        .get()
                                        .await()
                                    
                                    if (!existingQuery.isEmpty) {
                                        // Update existing entry
                                        val doc = existingQuery.documents.first()
                                        doc.reference.update(
                                            "days", userDays,
                                            "timestamp", Date()
                                        ).await()
                                    } else {
                                        // Add new entry
                                        db.collection("leaderboard").add(
                                            mapOf(
                                                "user" to userName, 
                                                "days" to userDays, 
                                                "timestamp" to Date()
                                            )
                                        ).await()
                                    }
                                    
                                    if (userDays == 0) {
                                        snackbarHostState.showSnackbar("✅ Joined the leaderboard! Start your smoke-free journey today!")
                                    } else {
                                        snackbarHostState.showSnackbar("✅ Progress shared! Your $userDays days are now on the leaderboard!")
                                    }
                                    
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("❌ Failed to share progress: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 8.dp),
                        enabled = !isLoading // Remove the userDays > 0 condition
                    ) {
                        Text(if (userDays == 0) "Join Leaderboard" else "Share Your Progress")
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${index + 1} ", fontWeight = FontWeight.Bold)
                                    Text(entry.user)
                                    if (entry.user == userName) {
                                        Text(" (You)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("${entry.days} days", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                1 -> {
                    // Community Tab Content
                    Text("Community Chat", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Share your journey and support others! 💪",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Messages List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(communityMessages) { index, message ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (message.user == userName) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            message.user,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                                                .format(message.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        message.message,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Message Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessage,
                            onValueChange = { newMessage = it },
                            placeholder = { Text("Type your message...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newMessage.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            db.collection("community_messages").add(
                                                mapOf(
                                                    "user" to userName,
                                                    "message" to newMessage,
                                                    "timestamp" to Date()
                                                )
                                            ).await()
                                            newMessage = ""
                                            snackbarHostState.showSnackbar("✅ Message sent!")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("❌ Failed to send message: ${e.message}")
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}
