@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.quitesmoking

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import java.time.LocalDate
import java.time.ZoneId
import com.example.quitesmoking.model.MilestoneGoal
import com.example.quitesmoking.repo.MilestoneGoalRepository
import com.example.quitesmoking.repo.DailySmokingStatusRepository
import com.example.quitesmoking.model.DailySmokingStatus
import com.example.quitesmoking.ui.SmokingCalendarView
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.quitesmoking.navigation.goHomeInTabs
@Composable
fun StatsScreen(navController: NavController, bottomNav: NavController) {
    androidx.activity.compose.BackHandler { bottomNav.goHomeInTabs() }

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()
    
    var smokeFreeTarget by remember { mutableIntStateOf(0) }
    var maxUsageTarget by remember { mutableIntStateOf(0) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("This Week") }
    var smokeFreeDays by remember { mutableIntStateOf(0) }
    var estimatedSavings by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var currentGoal by remember { mutableStateOf<MilestoneGoal?>(null) }
    var goalId by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var smokingStatuses by remember { mutableStateOf<List<DailySmokingStatus>>(emptyList()) }
    var totalSmokeFreeDays by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    
    val ranges = listOf("This Week", "This Month", "All Time")

    // Calculate date ranges
    val today = remember { Calendar.getInstance() }
    val firstDay = remember { Calendar.getInstance() }
    val lastDay = remember { Calendar.getInstance() }
    
    // Update date ranges when selectedRange changes
    LaunchedEffect(selectedRange) {
        firstDay.time = today.time
        lastDay.time = today.time
        
        when (selectedRange) {
            "This Week" -> {
                firstDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                lastDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            }
            "This Month" -> {
                firstDay.set(Calendar.DAY_OF_MONTH, 1)
                lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            "All Time" -> {
                firstDay.set(Calendar.YEAR, 2020) // Set to a reasonable start date
                // lastDay remains as today
            }
        }
    }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    // Load current milestone goal and historical data
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                // Load current goal
                val goal = MilestoneGoalRepository.getCurrentMilestoneGoal()
                currentGoal = goal
                if (goal != null) {
                    smokeFreeTarget = goal.smokeFreeTarget
                    maxUsageTarget = goal.maxUsageTarget
                    selectedRange = goal.goalPeriod
                    // We need to get the goal ID for updates - for now we'll create a new one
                    // In a production app, you'd want to store and retrieve the actual document ID
                }
                
                // Update total smoke-free days from goal if available
                if (goal?.totalSmokeFreeDays != null && goal.totalSmokeFreeDays > 0) {
                    totalSmokeFreeDays = goal.totalSmokeFreeDays
                }
                
                // Load total smoke-free days and current streak
                totalSmokeFreeDays = DailySmokingStatusRepository.getTotalSmokeFreeDays()
                currentStreak = DailySmokingStatusRepository.getCurrentStreak()
                
            } catch (e: Exception) {
                // Handle error silently, use default values
            }
        }
    }

    // Load smoking statuses for calendar view
    LaunchedEffect(selectedDate, userId) {
        if (userId != null) {
            try {
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                
                smokingStatuses = DailySmokingStatusRepository.getSmokeFreeDaysForMonth(year, month)
            } catch (e: Exception) {
                smokingStatuses = emptyList()
            }
        }
    }

    // Load data based on selected range
    LaunchedEffect(selectedRange, userId) {
        if (userId != null) {
            isLoading = true
            try {
                val startDate = firstDay.time
                val endDate = lastDay.time
                
                val query = db.collection("users")
                    .document(userId)
                    .collection("dailyCheckResponses")
                    .whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)
                
                val snapshot = query.get().await()
                
                val smokeFreeCount = snapshot.documents.count { doc ->
                    val answer = doc.getString("answer")
                    answer?.lowercase()?.contains("no") == true || answer == "否"
                }
                
                smokeFreeDays = smokeFreeCount
                
                // Calculate estimated savings based on user's goal or actual smoke-free days
                val daysToCalculate = if (smokeFreeTarget > 0) smokeFreeTarget else smokeFreeCount
                
                // Calculate cost per day based on typical smoking costs
                // This could be made configurable by the user in the future
                val costPerDay = 10.0 // Default: $10 per day
                estimatedSavings = daysToCalculate * costPerDay
                
            } catch (e: Exception) {
                // Handle error
                smokeFreeDays = 0
                estimatedSavings = 0.0
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestone Tracker") },
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
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Weekly Goal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Goal for the Week", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${dateFormat.format(firstDay.time)} - ${dateFormat.format(lastDay.time)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅ Smoke-free days target: ", fontSize = 16.sp)
                        Text("$smokeFreeTarget", fontSize = 16.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚫 Max e-cig uses: ", fontSize = 16.sp)
                        Text("$maxUsageTarget", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress indicator
                    if (smokeFreeTarget > 0) {
                        val progress = (smokeFreeDays.toFloat() / smokeFreeTarget.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Progress: $smokeFreeDays / $smokeFreeTarget days (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Button(onClick = { isEditing = true }) {
                        Text("Edit Goals")
                    }
                }
            }

            // Overall Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Overall Progress", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalSmokeFreeDays.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Total Smoke-free Days",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentStreak.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = "Current Streak",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Calendar View
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📅 Calendar View", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Quick stats for the current month
                        val currentMonthStatuses = smokingStatuses.filter { status ->
                            val statusCal = Calendar.getInstance().apply { time = status.date }
                            val currentCal = Calendar.getInstance().apply { time = selectedDate }
                            statusCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                            statusCal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)
                        }
                        
                        val smokeFreeCount = currentMonthStatuses.count { it.isSmokeFree }
                        val totalDays = currentMonthStatuses.size
                        
                        if (totalDays > 0) {
                            Text(
                                text = "$smokeFreeCount/$totalDays smoke-free",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SmokingCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = date
                        },
                        smokingStatuses = smokingStatuses,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp) // Increased height for better visibility
                    )
                }
            }
            
            // Selected Date Details
            val selectedDateStatus = smokingStatuses.find { status ->
                val statusCal = Calendar.getInstance().apply { time = status.date }
                val selectedCal = Calendar.getInstance().apply { time = selectedDate }
                statusCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                statusCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                statusCal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)
            }
            
            if (selectedDateStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Details for ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(selectedDate)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedDateStatus.isSmokeFree) "✅ Smoke-free" else "❌ Smoked",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedDateStatus.isSmokeFree) Color(0xFF4CAF50) else Color(0xFFFF5722)
                            )
                        }
                        
                        selectedDateStatus.notes?.let { notes ->
                            if (notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Notes: $notes", fontSize = 14.sp)
                            }
                        }
                        
                        selectedDateStatus.mood?.let { mood ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mood: $mood/5", fontSize = 14.sp)
                        }
                        
                        if (selectedDateStatus.triggers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Triggers: ${selectedDateStatus.triggers.joinToString(", ")}", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Money Saved Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💰 Money Saved", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ranges.forEach { range ->
                            FilterChip(
                                selected = selectedRange == range,
                                onClick = { 
                                    selectedRange = range
                                    // Update goals when range changes
                                    scope.launch {
                                        try {
                                            if (currentGoal != null) {
                                                val updatedGoal = currentGoal!!.copy(
                                                    goalPeriod = range,
                                                    startDate = firstDay.time,
                                                    endDate = lastDay.time
                                                )
                                                if (goalId != null) {
                                                    MilestoneGoalRepository.updateMilestoneGoal(goalId!!, updatedGoal)
                                                } else {
                                                    MilestoneGoalRepository.activateNewGoal(updatedGoal)
                                                }
                                                currentGoal = updatedGoal
                                            }
                                        } catch (e: Exception) {
                                            // Handle error silently
                                        }
                                    }
                                },
                                label = { Text(range) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${dateFormat.format(firstDay.time)} – ${dateFormat.format(lastDay.time)}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Text("Smoke-free days:", fontSize = 16.sp)
                        Text(smokeFreeDays.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show goal-based savings if user has set goals
                        if (smokeFreeTarget > 0) {
                            val costPerDay = 10.0 // Should match the calculation above
                            val goalSavings = smokeFreeTarget * costPerDay
                            Text("Goal-based savings:", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                "$${"%.2f".format(goalSavings)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Text("Estimated money saved:", fontSize = 16.sp)
                        Text(
                            "$${"%.2f".format(estimatedSavings)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        
                        // Show additional info if user has goals
                        if (smokeFreeTarget > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val remainingDays = (smokeFreeTarget - smokeFreeDays).coerceAtLeast(0)
                            if (remainingDays > 0) {
                                Text(
                                    text = "Remaining to goal: $remainingDays days",
                                    fontSize = 14.sp,
                                    color = Color(0xFFFF9800)
                                )
                            } else {
                                Text(
                                    text = "🎉 Goal achieved!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Goal Dialog
    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                // Create new milestone goal
                                val newGoal = MilestoneGoal(
                                    smokeFreeTarget = smokeFreeTarget,
                                    maxUsageTarget = maxUsageTarget,
                                    goalPeriod = selectedRange,
                                    startDate = firstDay.time,
                                    endDate = lastDay.time,
                                    createdAt = Date(),
                                    isActive = true,
                                    totalSmokeFreeDays = totalSmokeFreeDays,
                                    lastUpdated = Date()
                                )
                                
                                // Save to Firestore
                                if (goalId != null) {
                                    MilestoneGoalRepository.updateMilestoneGoal(goalId!!, newGoal)
                                } else {
                                    MilestoneGoalRepository.activateNewGoal(newGoal)
                                }
                                
                                // Update local state
                                currentGoal = newGoal
                                isEditing = false
                            } catch (e: Exception) {
                                // Handle error - could show a snackbar here
                                isEditing = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit Milestone Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = smokeFreeTarget.toString(),
                        onValueChange = {
                            smokeFreeTarget = it.toIntOrNull() ?: 0
                        },
                        label = { Text("Smoke-free days target") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxUsageTarget.toString(),
                        onValueChange = {
                            maxUsageTarget = it.toIntOrNull() ?: 0
                        },
                        label = { Text("Max e-cig uses") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Goal Period: $selectedRange",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Period: ${dateFormat.format(firstDay.time)} - ${dateFormat.format(lastDay.time)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        )
    }
}