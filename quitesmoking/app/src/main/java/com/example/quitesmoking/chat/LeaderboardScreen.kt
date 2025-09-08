package com.example.quitesmoking.chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Function to filter leaderboard entries by selected date range
private fun filterEntriesByDateRange(entries: List<LeaderboardEntry>, range: String): List<LeaderboardEntry> {
    return try {
        // If range is "All Time" or empty, return all entries
        if (range.isBlank() || range == "All Time") return entries
        
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
        
        // Add one day to end date to make the range inclusive
        endCal.add(Calendar.DATE, 1)
        
        Log.d("LeaderboardScreen", "Filtering entries between ${format.format(startCal.time)} and ${format.format(endCal.time)}")
        
        // Filter entries that fall within the date range
        entries.filter { entry ->
            val entryCal = Calendar.getInstance().apply { time = entry.timestamp }
            val isInRange = entryCal.timeInMillis >= startCal.timeInMillis && 
                           entryCal.timeInMillis < endCal.timeInMillis
            
            if (isInRange) {
                Log.d("LeaderboardScreen", "Entry in range: ${entry.user}, date: ${format.format(entry.timestamp)}")
            }
            
            isInRange
        }
    } catch (e: Exception) {
        Log.e("LeaderboardScreen", "Error filtering by date range: ${e.message}", e)
        // If parsing fails, return all entries
        entries
    }
}

@Composable
fun LeaderboardScreen() {
    val db = FirebaseFirestore.getInstance()
    var allLeaderboardEntries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Date range selection
    val today = remember { Calendar.getInstance() }
    val format = SimpleDateFormat("MMM d", Locale.getDefault())
    var selectedRange by remember { mutableStateOf("All Time") }
    var dateMenuExpanded by remember { mutableStateOf(false) }
    
    // Generate date options for the dropdown with more meaningful ranges
    val dateOptions = remember {
        val options = mutableListOf("All Time")
        
        // Last 7 days
        val lastWeekStart = Calendar.getInstance().apply { add(Calendar.DATE, -7) }
        options.add("${format.format(lastWeekStart.time)} - ${format.format(today.time)} (Last 7 days)")
        
        // Last 30 days
        val lastMonthStart = Calendar.getInstance().apply { add(Calendar.DATE, -30) }
        options.add("${format.format(lastMonthStart.time)} - ${format.format(today.time)} (Last 30 days)")
        
        // This month
        val thisMonthStart = Calendar.getInstance().apply { 
            set(Calendar.DAY_OF_MONTH, 1)
        }
        options.add("${format.format(thisMonthStart.time)} - ${format.format(today.time)} (This Month)")
        
        // Last month
        val lastMonthEnd = Calendar.getInstance().apply { 
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DATE, -1)
        }
        val lastMonthFirstDay = Calendar.getInstance().apply { 
            time = lastMonthEnd.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        options.add("${format.format(lastMonthFirstDay.time)} - ${format.format(lastMonthEnd.time)} (Last Month)")
        
        options
    }

    LaunchedEffect(Unit) {
        try {
            // Fetch leaderboard data from Firestore with real-time updates
            db.collection("leaderboard")
                .orderBy("days", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("LeaderboardScreen", "Error listening to leaderboard updates", e)
                        errorMessage = "⚠️ Failed to load data. Please check your Internet connection."
                        isLoading = false
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
                        
                        // Apply current filter
                        if (selectedRange == "All Time") {
                            leaderboard = entries
                        } else {
                            val filteredEntries = filterEntriesByDateRange(entries, selectedRange)
                            leaderboard = filteredEntries
                        }
                        
                        isLoading = false
                    }
                }
        } catch (e: Exception) {
            Log.e("LeaderboardScreen", "Error setting up leaderboard listener", e)
            errorMessage = "⚠️ Failed to load data. Please check your Internet connection."
            isLoading = false
        }
    }
    
    // Apply filtering when selected range changes
    LaunchedEffect(selectedRange, allLeaderboardEntries) {
        if (allLeaderboardEntries.isNotEmpty()) {
            if (selectedRange == "All Time") {
                leaderboard = allLeaderboardEntries
                Log.d("LeaderboardScreen", "Showing all ${allLeaderboardEntries.size} entries")
            } else {
                val filteredEntries = filterEntriesByDateRange(allLeaderboardEntries, selectedRange)
                leaderboard = filteredEntries
                Log.d("LeaderboardScreen", "Filtered to ${filteredEntries.size} entries for range: $selectedRange")
                
                // Debug timestamp information for first few entries
                allLeaderboardEntries.take(3).forEachIndexed { index, entry ->
                    Log.d("LeaderboardScreen", "Entry $index: ${entry.user}, timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(entry.timestamp)}")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.titleLarge)
        
        // Display current filter information
        if (selectedRange != "All Time") {
            Text(
                "Showing entries for: $selectedRange",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date range dropdown
        Box {
            OutlinedButton(
                onClick = { dateMenuExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedRange)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select date range")
            }
            
            DropdownMenu(
                expanded = dateMenuExpanded,
                onDismissRequest = { dateMenuExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                dateOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedRange = option
                            dateMenuExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            leaderboard.isEmpty() -> Text("No leaderboard data available for the selected date range.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(leaderboard) { index, entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${index + 1} ", fontWeight = FontWeight.Bold)
                                Text(entry.user)
                            }
                            Text("${entry.days} days", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class LeaderboardEntry(
    val user: String,
    val days: Int,
    val timestamp: Date = Date()
)
