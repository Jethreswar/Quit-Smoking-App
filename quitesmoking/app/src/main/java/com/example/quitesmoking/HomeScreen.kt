package com.example.quitesmoking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.quitesmoking.navigation.Routes
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
/* check for firebase*/

/* Firebase Setup Announcement*/


/* DataStore setup */
private val Context.dataStore by preferencesDataStore(name = "checkin_prefs")
private val LAST_CHECKIN_KEY = stringPreferencesKey("last_checkin_date")

data class GameApp(val name: String, val url: String)

private fun isCurrentlyMorning(): Boolean {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return hour in 2..20
}

@Composable
fun RecommendedGamesSection() {
    val context = LocalContext.current
    val recommendedApps = listOf(
        GameApp("Mind Games", "https://play.google.com/store/apps/details?id=com.reflex.mindgames"),
        GameApp("QuitNow!", "https://play.google.com/store/apps/details?id=net.eaginsoftware.android.quitsmoking"),
        GameApp("Breathe Easy", "https://play.google.com/store/apps/details?id=com.breatheeasy.app")
    )

    Column {
        Text("Try These Games", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recommendedApps) { app ->
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = Color(0xFF673AB7))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(app.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi", "UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    var moneySaved by remember { mutableDoubleStateOf(0.0) }
    var smokeFreeDays by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val purchaseSnapshot = db.collection("users")
                .document(userId)
                .collection("purchases")
                .get()
                .await()
            val totalSpent = purchaseSnapshot.documents.sumOf { it.getDouble("price") ?: 0.0 }
            moneySaved = totalSpent * 5

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
                    if (date != null && (answer == "No" || answer.equals("no", ignoreCase = true))) date else null
                }
                .sortedDescending()

            smokeFreeDays = sorted.size

            // Calculate current streak
            var streak = 0
            var today = LocalDate.now()
            for (date in sorted) {
                if (date == today || date == today.minusDays(1)) {
                    streak++
                    today = today.minusDays(1)
                } else {
                    break
                }
            }
            currentStreak = streak
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .padding(bottom = 88.dp) // Add padding for bottom navigation bar
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Button group in top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TopButtonGroup(navController = navController)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F5)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GreetingMessageWithCheckinButton()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Days Smoke-Free", style = MaterialTheme.typography.bodyMedium)
                            Text("$smokeFreeDays", fontSize = 32.sp, color = Color(0xFF3F51B5), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Streak", style = MaterialTheme.typography.bodyMedium)
                            Text("$currentStreak", fontSize = 32.sp, color = Color(0xFFFF5722), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Money Saved", style = MaterialTheme.typography.bodyLarge)
                    Text("$${"%.2f".format(moneySaved)}", fontSize = 24.sp, color = Color(0xFFFF7043))
                }
            }

//            Button(
//                onClick = { showDialog = true },
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Log purchase")
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Log Purchase")
//            }

            if (showDialog) {
                PurchaseDialog(
                    lastBrand = null,
                    lastPrice = null,
                    onDismiss = { showDialog = false },
                    onSave = { p ->
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId!!)
                            .collection("purchases")
                            .add(p)
                        showDialog = false
                    }
                )
            }



            // Dashboard navigation icons
//            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    DynamicCheckInButton(navController)
//
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    DynamicCheckInButton(navController)
//
//                }
//            }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F5)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Daily Motivation", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "\"Every time you resist a craving, you're one step closer to freedom.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "- Your Future Self",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF3F51B5),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            RecommendedGamesSection()
        }
    }
}

@Composable
fun DashboardIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontSize = 12.sp)
        }
    }
}



/* ============Daily Check In at Different Time Periods (three checkin buttons) =========== */

@Composable
fun GreetingMessageWithCheckinButton() {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (greeting, labelPrefix) = when (hour) {
        in 0..11 -> "Good Morning\n \n Ready for your morning check-in?" to "Morning"
        in 12..17 -> "Good Afternoon\n \n Ready for your afternoon check-in?" to "Afternoon"
        else -> "Good Evening\n \nReady for your evening check-in?" to "Evening"
    }

    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val userName = user?.displayName ?: "Anonymous"

    // （Aug 3）
    val format = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val today = remember { Calendar.getInstance() }
    val formattedToday = remember { format.format(today.time) }

    var checkedIn by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Daily $labelPrefix Check-in") }

    // ✅ whether Check-in or not
    LaunchedEffect(Unit) {
        if (userId != null) {
//            val todayStart = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, 0)
//                set(Calendar.MINUTE, 0)
//                set(Calendar.SECOND, 0)
//                set(Calendar.MILLISECOND, 0)
//            }.time

//            val snapshot = db.collection("checkins")
//                .whereEqualTo("userId", userId)
//                .whereGreaterThanOrEqualTo("timestamp", todayStart)
//                .get()
//                .await()
            val snapshot = db.collection("checkins")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                checkedIn = true
                buttonText = "$labelPrefix Check-in on $formattedToday Complete!"
            }
        }
    }

    // ✅ UI display
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Today is $formattedToday",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (!checkedIn && userId != null) {
                    scope.launch {
                        // Step 1: write check-in
                        db.collection("checkins").add(
                            mapOf(
                                "userId" to userId,
                                "userName" to userName,
                                "timestamp" to Date(),
                                "label" to labelPrefix,
                                "dateFormatted" to formattedToday
                            )
                        )

                        // Step 2: update leaderboard
                        val leaderboardRef = db.collection("leaderboard")
                        val existing = leaderboardRef
                            .whereEqualTo("user", userName)
                            .get()
                            .await()

                        if (!existing.isEmpty) {
                            val doc = existing.documents.first()
                            val currentDays = doc.getLong("days") ?: 0
                            leaderboardRef.document(doc.id).update(
                                "days", currentDays + 1,
                                "timestamp", Date()
                            )
                        } else {
                            leaderboardRef.add(
                                mapOf(
                                    "user" to userName,
                                    "days" to 1,
                                    "timestamp" to Date()
                                )
                            )
                        }

                        // Step 3: update status
                        checkedIn = true
                        buttonText = "$labelPrefix Check-in on $formattedToday Complete!"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (checkedIn) Color(0xFFB2DFDB) else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = buttonText,
                color = if (checkedIn) Color.Black else Color.White
            )
        }
    }
}



/* the previous one + the new on */
//@Composable
//fun GreetingMessageWithCheckinButton() {
//    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
//    val (greeting, labelPrefix) = when (hour) {
//        in 0..11 -> "Good Morning\n \n Ready for your morning check-in?" to "Morning"
//        in 12..17 -> "Good Afternoon\n \n Ready for your afternoon check-in?" to "Afternoon"
//        else -> "Good Evening\n \nReady for your evening check-in?" to "Evening"
//    }
//
//    var checkedIn by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = greeting,
//            style = MaterialTheme.typography.titleMedium,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = { checkedIn = true },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 32.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (checkedIn) Color(0xFFB2DFDB) else MaterialTheme.colorScheme.primary
//            )
//        ) {
//            Text(
//                text = if (checkedIn) "$labelPrefix Check-in Complete!" else "Daily $labelPrefix Check-in",
//                color = if (checkedIn) Color.Black else Color.White
//            )
//        }
//    }
//}

/* ============ Dynamic Morning/Night Check-in Button ============ */
@SuppressLint("NewApi")
@Composable
fun DynamicCheckInButton(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val now = LocalDateTime.now()
    val isMorning = now.hour in 2..20
    val label = if (isMorning) "Morning" else "Night"
    val route = if (isMorning) Routes.MORNING_CHECK else Routes.NIGHT_CHECK
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val periodKey = "$label-$todayStr"

    var lastCheckInKey by remember { mutableStateOf<String?>(null) }

    // Refresh every time composition happens (e.g., time changes)
    LaunchedEffect(label, todayStr) {
        val prefs = context.dataStore.data.first()
        lastCheckInKey = prefs[LAST_CHECKIN_KEY]
    }

    val alreadyDone = lastCheckInKey == periodKey
    val dynamicIcon = if (isMorning) Icons.Default.WbSunny else Icons.Default.NightsStay

    val onClick: () -> Unit = {
        if (!alreadyDone) {
            navController.navigate(route)
            scope.launch {
                context.dataStore.edit { it[LAST_CHECKIN_KEY] = periodKey }
            }
        }
    }

    Card(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        enabled = !alreadyDone,
        colors = CardDefaults.cardColors(
            containerColor = if (alreadyDone) Color(0xFFEEEEEE)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = dynamicIcon,
                contentDescription = label,
                tint = if (alreadyDone) Color.Gray else LocalContentColor.current
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (alreadyDone) Color.Gray else LocalContentColor.current
            )
        }
    }
}

@Composable
fun TopButtonGroup(navController: NavController) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Morning/Night Check-in Button
        IconButton(
            onClick = {
                navController.navigate(if (isCurrentlyMorning()) Routes.MORNING_CHECK else Routes.NIGHT_CHECK)
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isCurrentlyMorning()) Icons.Default.WbSunny else Icons.Default.NightsStay,
                contentDescription = if (isCurrentlyMorning()) "Morning Check-in" else "Night Check-in",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Settings Button
        IconButton(
            onClick = {
                // TODO: Navigate to settings screen
                // navController.navigate(Routes.SETTINGS)
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Quick Help Button
        IconButton(
            onClick = {
                // TODO: Navigate to help screen or show help dialog
                // navController.navigate(Routes.HELP)
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Help,
                contentDescription = "Quick Help",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}