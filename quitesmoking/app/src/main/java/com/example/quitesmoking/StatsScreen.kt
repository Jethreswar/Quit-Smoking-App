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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(navController: NavController) {
    var smokeFreeTarget by remember { mutableIntStateOf(0) }
    var maxUsageTarget by remember { mutableIntStateOf(0) }
    var isEditing by remember { mutableStateOf(false) }

    // 当前周
    val today = remember { Calendar.getInstance() }
    val firstDay = today.clone() as Calendar
    val lastDay = today.clone() as Calendar
    firstDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    lastDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    // 示例统计数据
    val smokeFreeDays = 0
    val estimatedSavings = 0.00
    var selectedRange by remember { mutableStateOf("This Week") }
    val ranges = listOf("This Week", "This Month", "All Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Tracker") },
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
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 添加滑动
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
                    Button(onClick = { isEditing = true }) {
                        Text("Edit")
                    }
                }
            }

            // Calendar View
            Text("Calendar View", style = MaterialTheme.typography.titleSmall)
            AndroidView(factory = { context ->
                CalendarView(context).apply {
                    minDate = firstDay.timeInMillis
                    maxDate = lastDay.timeInMillis
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp))

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
                                onClick = { selectedRange = range },
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
                    Text("Smoke-free days:", fontSize = 16.sp)
                    Text(smokeFreeDays.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estimated money saved:", fontSize = 16.sp)
                    Text(
                        text = "$${"%.2f".format(estimatedSavings)}",
                        fontSize = 20.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Edit Goal Dialog
    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            confirmButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit Weekly Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = smokeFreeTarget.toString(),
                        onValueChange = {
                            smokeFreeTarget = it.toIntOrNull() ?: 0
                        },
                        label = { Text("Smoke-free days") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxUsageTarget.toString(),
                        onValueChange = {
                            maxUsageTarget = it.toIntOrNull() ?: 0
                        },
                        label = { Text("Max e-cig uses") },
                        singleLine = true
                    )
                }
            }
        )
    }
}

//@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
//package com.example.quitesmoking
//
//import android.widget.CalendarView
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.navigation.NavController
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun StatsScreen(navController: NavController) {
//    var smokeFreeTarget by remember { mutableIntStateOf(0) }
//    var maxUsageTarget by remember { mutableIntStateOf(0) }
//    var isEditing by remember { mutableStateOf(false) }
//
//    val today = remember { Calendar.getInstance() }
//    val firstDay = today.clone() as Calendar
//    val lastDay = today.clone() as Calendar
//    firstDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
//    lastDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
//
//    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Goal for the Week") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
//                elevation = CardDefaults.cardElevation(4.dp)
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text("Goal for the Week", style = MaterialTheme.typography.titleMedium)
//                    Text(
//                        text = "${dateFormat.format(firstDay.time)} - ${dateFormat.format(lastDay.time)}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("✅ Smoke-free days target: ", fontSize = 16.sp)
//                        Text("$smokeFreeTarget", fontSize = 16.sp)
//                    }
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("🚫 Max e-cig uses: ", fontSize = 16.sp)
//                        Text("$maxUsageTarget", fontSize = 16.sp)
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Button(onClick = { isEditing = true }) {
//                        Text("Edit")
//                    }
//                }
//            }
//
//            Text("Calendar View", style = MaterialTheme.typography.titleSmall)
//            AndroidView<CalendarView>(factory = { context ->
//                CalendarView(context).apply {
//                    minDate = firstDay.timeInMillis
//                    maxDate = lastDay.timeInMillis
//                }
//            }, modifier = Modifier.fillMaxWidth().height(300.dp))
//        }
//    }
//
//    if (isEditing) {
//        AlertDialog(
//            onDismissRequest = { isEditing = false },
//            confirmButton = {
//                TextButton(onClick = { isEditing = false }) {
//                    Text("Save")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { isEditing = false }) {
//                    Text("Cancel")
//                }
//            },
//            title = { Text("Edit Weekly Goals") },
//            text = {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    OutlinedTextField(
//                        value = smokeFreeTarget.toString(),
//                        onValueChange = {
//                            smokeFreeTarget = it.toIntOrNull() ?: 0
//                        },
//                        label = { Text("Smoke-free days") },
//                        singleLine = true
//                    )
//                    OutlinedTextField(
//                        value = maxUsageTarget.toString(),
//                        onValueChange = {
//                            maxUsageTarget = it.toIntOrNull() ?: 0
//                        },
//                        label = { Text("Max e-cig uses") },
//                        singleLine = true
//                    )
//                }
//            }
//        )
//    }
//}



//@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
//package com.example.quitesmoking
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.ThumbUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//import java.text.SimpleDateFormat
//import java.util.*
//
//data class SmokeFreeData(val date: String, val cumulativeSmokeFree: Int, val usageEstimate: Int?)
//data class BadgeInfo(val name: String, val icon: ImageVector, val requiredDays: Int, val current: Int)
//
//@Composable
//fun StatsScreen(navController: NavController) {
//    val db = FirebaseFirestore.getInstance()
//    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//    var chartData by remember { mutableStateOf<List<SmokeFreeData>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var selectedTab by remember { mutableStateOf("All Time") }
//    var cumulativeSmokeFree by remember { mutableIntStateOf(0) }
//
//    val tabs = listOf("Weekly", "Monthly", "All Time")
//
//    LaunchedEffect(Unit) {
//        val snapshot = db.collection("users").document(userId)
//            .collection("dailyCheckResponses").get().await()
//
//        val sorted = snapshot.documents.sortedBy { it.getTimestamp("timestamp")?.toDate() }
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        val data = mutableListOf<SmokeFreeData>()
//        var cumulative = 0
//        for (doc in sorted) {
//            val date = sdf.format(doc.getTimestamp("timestamp")?.toDate() ?: continue)
//            val answer = doc.getString("answer")
//            val questionId = doc.getString("questionId")
//            val usageEstimate = when (answer) {
//                "1–5" -> 3
//                "6–10" -> 8
//                "10+" -> 12
//                else -> null
//            }
//            if (questionId == "2" && (answer == "否" || answer?.lowercase()?.contains("no") == true)) {
//                cumulative++
//                data.add(SmokeFreeData(date, cumulative, null))
//            } else if (questionId == "2.1") {
//                data.add(SmokeFreeData(date, cumulative, usageEstimate))
//            }
//        }
//        chartData = data
//        cumulativeSmokeFree = cumulative
//        isLoading = false
//    }
//
//    val badges = listOf(
//        BadgeInfo("One Day Free", Icons.Default.ThumbUp, 1, cumulativeSmokeFree),
//        BadgeInfo("Health Master", Icons.Default.Favorite, 30, cumulativeSmokeFree)
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Progress Tracker") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
//                tabs.forEachIndexed { index, tab ->
//                    Tab(
//                        selected = selectedTab == tab,
//                        onClick = { selectedTab = tab },
//                        text = { Text(tab) }
//                    )
//                }
//            }
//
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//            } else {
//                val filteredData = when (selectedTab) {
//                    "Weekly" -> chartData.takeLast(7)
//                    "Monthly" -> chartData.takeLast(30)
//                    else -> chartData
//                }
//                StatsLineChart(filteredData)
//                Spacer(Modifier.height(8.dp))
//                UsageBarChart(filteredData)
//                Spacer(Modifier.height(8.dp))
//                Text("Your Graph, Your Story!", fontSize = 16.sp)
//
//                Spacer(Modifier.height(16.dp))
//                Text("Badges Earned", style = MaterialTheme.typography.titleMedium)
//                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                    badges.forEach { badge ->
//                        BadgeCard(badge = badge, unlocked = cumulativeSmokeFree >= badge.requiredDays)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun StatsLineChart(data: List<SmokeFreeData>) {
//    val maxY = (data.maxOfOrNull { it.cumulativeSmokeFree } ?: 10).coerceAtLeast(5)
//    val padding = 32.dp
//
//    Canvas(modifier = Modifier
//        .fillMaxWidth()
//        .height(160.dp)) {
//        val width = size.width - padding.toPx()
//        val height = size.height - padding.toPx()
//        val xStep = width / (data.size - 1).coerceAtLeast(1)
//        val yStep = height / maxY
//
//        drawLine(Color.Gray, Offset(padding.toPx(), 0f), Offset(padding.toPx(), height), strokeWidth = 2f)
//        drawLine(Color.Gray, Offset(padding.toPx(), height), Offset(size.width, height), strokeWidth = 2f)
//
//        val pathEffect = PathEffect.cornerPathEffect(10f)
//        for (i in 0 until data.size - 1) {
//            val s = Offset(padding.toPx() + i * xStep, height - data[i].cumulativeSmokeFree * yStep)
//            val e = Offset(padding.toPx() + (i + 1) * xStep, height - data[i + 1].cumulativeSmokeFree * yStep)
//            drawLine(Color(0xFF4CAF50), start = s, end = e, strokeWidth = 4f, pathEffect = pathEffect)
//        }
//    }
//}
//
//@Composable
//fun UsageBarChart(data: List<SmokeFreeData>) {
//    val barWidth = 16.dp
//    val maxUsage = (data.maxOfOrNull { it.usageEstimate ?: 0 } ?: 5).coerceAtLeast(5)
//    val height = 100.dp
//
//    Canvas(modifier = Modifier
//        .fillMaxWidth()
//        .height(height)) {
//        val spacing = 24f
//        val xStep = spacing + barWidth.toPx()
//
//        data.forEachIndexed { index, item ->
//            item.usageEstimate?.let { usage ->
//                val barHeight = (usage / maxUsage.toFloat()) * size.height
//                drawRect(
//                    color = Color(0xFFFF7043),
//                    topLeft = Offset(index * xStep, size.height - barHeight),
//                    size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeight)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun BadgeCard(badge: BadgeInfo, unlocked: Boolean) {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier.size(100.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Icon(
//                imageVector = badge.icon,
//                contentDescription = badge.name,
//                tint = if (unlocked) Color(0xFF4CAF50) else Color.Gray,
//                modifier = Modifier.size(32.dp)
//            )
//            Spacer(Modifier.height(4.dp))
//            Text(badge.name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
//            if (unlocked) {
//                Text("Unlocked!", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
//            } else {
//                Text("${badge.current}/${badge.requiredDays}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
//            }
//        }
//    }
//}@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
//package com.example.quitesmoking
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.ThumbUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//import java.text.SimpleDateFormat
//import java.util.*
//
//data class SmokeFreeData(val date: String, val cumulativeSmokeFree: Int, val usageEstimate: Int?)
//data class BadgeInfo(val name: String, val icon: ImageVector, val requiredDays: Int, val current: Int)
//
//@Composable
//fun StatsScreen(navController: NavController) {
//    val db = FirebaseFirestore.getInstance()
//    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//    var chartData by remember { mutableStateOf<List<SmokeFreeData>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var selectedTab by remember { mutableStateOf("All Time") }
//    var cumulativeSmokeFree by remember { mutableIntStateOf(0) }
//
//    val tabs = listOf("Weekly", "Monthly", "All Time")
//
//    LaunchedEffect(Unit) {
//        val snapshot = db.collection("users").document(userId)
//            .collection("dailyCheckResponses").get().await()
//
//        val sorted = snapshot.documents.sortedBy { it.getTimestamp("timestamp")?.toDate() }
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        val data = mutableListOf<SmokeFreeData>()
//        var cumulative = 0
//        for (doc in sorted) {
//            val date = sdf.format(doc.getTimestamp("timestamp")?.toDate() ?: continue)
//            val answer = doc.getString("answer")
//            val questionId = doc.getString("questionId")
//            val usageEstimate = when (answer) {
//                "1–5" -> 3
//                "6–10" -> 8
//                "10+" -> 12
//                else -> null
//            }
//            if (questionId == "2" && (answer == "否" || answer?.lowercase()?.contains("no") == true)) {
//                cumulative++
//                data.add(SmokeFreeData(date, cumulative, null))
//            } else if (questionId == "2.1") {
//                data.add(SmokeFreeData(date, cumulative, usageEstimate))
//            }
//        }
//        chartData = data
//        cumulativeSmokeFree = cumulative
//        isLoading = false
//    }
//
//    val badges = listOf(
//        BadgeInfo("One Day Free", Icons.Default.ThumbUp, 1, cumulativeSmokeFree),
//        BadgeInfo("Health Master", Icons.Default.Favorite, 30, cumulativeSmokeFree)
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Progress Tracker") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
//                tabs.forEachIndexed { index, tab ->
//                    Tab(
//                        selected = selectedTab == tab,
//                        onClick = { selectedTab = tab },
//                        text = { Text(tab) }
//                    )
//                }
//            }
//
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//            } else {
//                val filteredData = when (selectedTab) {
//                    "Weekly" -> chartData.takeLast(7)
//                    "Monthly" -> chartData.takeLast(30)
//                    else -> chartData
//                }
//                StatsLineChart(filteredData)
//                Spacer(Modifier.height(8.dp))
//                UsageBarChart(filteredData)
//                Spacer(Modifier.height(8.dp))
//                Text("Your Graph, Your Story!", fontSize = 16.sp)
//
//                Spacer(Modifier.height(16.dp))
//                Text("Badges Earned", style = MaterialTheme.typography.titleMedium)
//                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                    badges.forEach { badge ->
//                        BadgeCard(badge = badge, unlocked = cumulativeSmokeFree >= badge.requiredDays)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun StatsLineChart(data: List<SmokeFreeData>) {
//    val maxY = (data.maxOfOrNull { it.cumulativeSmokeFree } ?: 10).coerceAtLeast(5)
//    val padding = 32.dp
//
//    Canvas(modifier = Modifier
//        .fillMaxWidth()
//        .height(160.dp)) {
//        val width = size.width - padding.toPx()
//        val height = size.height - padding.toPx()
//        val xStep = width / (data.size - 1).coerceAtLeast(1)
//        val yStep = height / maxY
//
//        drawLine(Color.Gray, Offset(padding.toPx(), 0f), Offset(padding.toPx(), height), strokeWidth = 2f)
//        drawLine(Color.Gray, Offset(padding.toPx(), height), Offset(size.width, height), strokeWidth = 2f)
//
//        val pathEffect = PathEffect.cornerPathEffect(10f)
//        for (i in 0 until data.size - 1) {
//            val s = Offset(padding.toPx() + i * xStep, height - data[i].cumulativeSmokeFree * yStep)
//            val e = Offset(padding.toPx() + (i + 1) * xStep, height - data[i + 1].cumulativeSmokeFree * yStep)
//            drawLine(Color(0xFF4CAF50), start = s, end = e, strokeWidth = 4f, pathEffect = pathEffect)
//        }
//    }
//}
//
//@Composable
//fun UsageBarChart(data: List<SmokeFreeData>) {
//    val barWidth = 16.dp
//    val maxUsage = (data.maxOfOrNull { it.usageEstimate ?: 0 } ?: 5).coerceAtLeast(5)
//    val height = 100.dp
//
//    Canvas(modifier = Modifier
//        .fillMaxWidth()
//        .height(height)) {
//        val spacing = 24f
//        val xStep = spacing + barWidth.toPx()
//
//        data.forEachIndexed { index, item ->
//            item.usageEstimate?.let { usage ->
//                val barHeight = (usage / maxUsage.toFloat()) * size.height
//                drawRect(
//                    color = Color(0xFFFF7043),
//                    topLeft = Offset(index * xStep, size.height - barHeight),
//                    size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeight)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun BadgeCard(badge: BadgeInfo, unlocked: Boolean) {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier.size(100.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Icon(
//                imageVector = badge.icon,
//                contentDescription = badge.name,
//                tint = if (unlocked) Color(0xFF4CAF50) else Color.Gray,
//                modifier = Modifier.size(32.dp)
//            )
//            Spacer(Modifier.height(4.dp))
//            Text(badge.name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
//            if (unlocked) {
//                Text("Unlocked!", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall)
//            } else {
//                Text("${badge.current}/${badge.requiredDays}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
//            }
//        }
//    }
//}