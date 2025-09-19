@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.quitesmoking

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.quitesmoking.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import java.util.Locale
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
    var selectedTab by remember { mutableStateOf(0) }

    val ranges = listOf("This Week", "This Month", "All Time")
    val tabs = listOf("Calendar", "Badges", "Forest", "Sailboat")

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Button(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,
                                contentColor = if (selectedTab == index)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index)
                                    androidx.compose.ui.text.font.FontWeight.Medium
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> CalendarTabContent(
                    smokeFreeTarget = smokeFreeTarget,
                    maxUsageTarget = maxUsageTarget,
                    smokeFreeDays = smokeFreeDays,
                    firstDay = firstDay,
                    lastDay = lastDay,
                    dateFormat = dateFormat,
                    onEditGoals = { isEditing = true },
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    smokingStatuses = smokingStatuses,
                    totalSmokeFreeDays = totalSmokeFreeDays,
                    currentStreak = currentStreak,
                    selectedRange = selectedRange,
                    onRangeChange = {
                        selectedRange = it
                        // Update goals when range changes
                        scope.launch {
                            try {
                                if (currentGoal != null) {
                                    val updatedGoal = currentGoal!!.copy(
                                        goalPeriod = it,
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
                    ranges = ranges,
                    isLoading = isLoading,
                    estimatedSavings = estimatedSavings
                )
                1 -> BadgesTabContent(totalSmokeFreeDays = totalSmokeFreeDays)
                2 -> ForestTabContent()
                3 -> SailboatTabContent()
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

@Composable
fun CalendarTabContent(
    smokeFreeTarget: Int,
    maxUsageTarget: Int,
    smokeFreeDays: Int,
    firstDay: Calendar,
    lastDay: Calendar,
    dateFormat: SimpleDateFormat,
    onEditGoals: () -> Unit,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    smokingStatuses: List<DailySmokingStatus>,
    totalSmokeFreeDays: Int,
    currentStreak: Int,
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    ranges: List<String>,
    isLoading: Boolean,
    estimatedSavings: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        progress = { progress },
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

                Button(onClick = onEditGoals) {
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
                    onDateSelected = onDateSelected,
                    smokingStatuses = smokingStatuses,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
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
                            onClick = { onRangeChange(range) },
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

@Composable
fun BadgesTabContent(totalSmokeFreeDays: Int) {
    // Define badge data
    val badges = listOf(
        BadgeData(
            title = "Day 1. Heart",
            description = "Your heart rate and blood pressure start to normalize",
            icon = "❤️",
            requiredDays = 1
        ),
        BadgeData(
            title = "Day 2. Lungs",
            description = "Carbon monoxide levels drop, oxygen levels improve",
            icon = "🫁",
            requiredDays = 2
        ),
        BadgeData(
            title = "Day 3. Brain",
            description = "Nicotine is completely out of your system",
            icon = "🧠",
            requiredDays = 3
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(250.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 Badges", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Unlock badges by staying smoke-free!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(25.dp))

                // Horizontal scrolling badge carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(badges) { badge ->
                        CarouselBadgeItem(
                            badge = badge,
                            isUnlocked = totalSmokeFreeDays >= badge.requiredDays,
                            totalSmokeFreeDays = totalSmokeFreeDays
                        )
                    }
                }
            }
        }
    }
}

data class BadgeData(
    val title: String,
    val description: String,
    val icon: String,
    val requiredDays: Int
)

@Composable
fun ForestTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌲 Forest Growth", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Your smoke-free journey grows a virtual forest!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Forest visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Color(0xFFE8F5E8),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🌳🌲🌿\n\nYour forest will grow here!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SailboatTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⛵ Sailboat Journey", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Navigate your way to a smoke-free life!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sailboat visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Color(0xFFE3F2FD),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⛵🌊🏝️\n\nYour journey awaits!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CarouselBadgeItem(
    badge: BadgeData,
    isUnlocked: Boolean,
    totalSmokeFreeDays: Int
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                Color(0xFFE8F5E8) 
            else 
                Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            if (isUnlocked) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: Badge icon and title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isUnlocked) 
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else 
                                Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.icon,
                        fontSize = 40.sp,
                        color = if (isUnlocked) Color(0xFF4CAF50) else Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Badge title
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.Black else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
            
            // Middle section: Description
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnlocked) 
                    MaterialTheme.colorScheme.onSurfaceVariant
                else 
                    Color.Gray,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            // Bottom section: Status and progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Progress indicator for locked badges
                if (!isUnlocked) {
                    Text(
                        text = "Progress: $totalSmokeFreeDays/${badge.requiredDays} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Unlock status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFF9E9E9E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "LOCKED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedBadgeItem(
    badge: BadgeData,
    isUnlocked: Boolean,
    totalSmokeFreeDays: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                Color(0xFFE8F5E8) 
            else 
                Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            if (isUnlocked) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (isUnlocked) 
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else 
                            Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 40.sp,
                    color = if (isUnlocked) Color(0xFF4CAF50) else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Badge content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.Black else Color.Gray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) 
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else 
                        Color.Gray,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress indicator
                if (!isUnlocked) {
                    Text(
                        text = "Progress: $totalSmokeFreeDays/${badge.requiredDays} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            // Unlock status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFF9E9E9E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "LOCKED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    isEarned: Boolean,
    icon: String
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) Color(0xFFE8F5E8) else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                color = if (isEarned) Color(0xFF4CAF50) else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (isEarned) Color.Black else Color.Gray
            )
        }
    }
}