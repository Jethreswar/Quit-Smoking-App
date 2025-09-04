@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.quitesmoking.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quitesmoking.repo.DailyLogRepository
import com.example.quitesmoking.navigation.Routes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*

private val emojiSet: List<Pair<Int, ImageVector>> = listOf(
    5 to Icons.Outlined.SentimentVerySatisfied,
    4 to Icons.Outlined.SentimentSatisfied,
    3 to Icons.Outlined.SentimentNeutral,
    2 to Icons.Outlined.SentimentDissatisfied,
    1 to Icons.Outlined.SentimentVeryDissatisfied,
)

@SuppressLint("NewApi")
@Composable
fun MorningCheckInScreen(nav: NavController) {
    val scope   = rememberCoroutineScope()
    val snack   = remember { SnackbarHostState() }
    val today   = LocalDate.now()
    val monday  = today.with(WeekFields.of(Locale.US).dayOfWeek(), 1)

    /* fetch weekly goals to display */
    var weeklyGoals by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            weeklyGoals = DailyLogRepository.getWeeklyGoal(monday)?.targets ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            weeklyGoals = emptyList()
        }
    }

    /* local UI state */
    var commit12h  by remember { mutableStateOf(true) }
    var mood       by remember { mutableStateOf<Int?>(null) }
    var whyKeep    by remember { mutableStateOf("") }
    var whatMatters by remember { mutableStateOf("") }
    var note       by remember { mutableStateOf("") }

     Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Good morning!") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val m = mood ?: return@ExtendedFloatingActionButton
                    scope.launch {
                        try {
                            DailyLogRepository.saveMorning(
                                today,
                                m,
                                note.takeIf { it.isNotBlank() }
                            )
                            snack.showSnackbar("Logged – stay smoke-free for 12 h!")
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            snack.showSnackbar("Save failed: ${e.message ?: "unknown error"}")
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            /* daily intention block */
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Goal for today:", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(commit12h, onCheckedChange = { commit12h = it })
                        Text("Stay smoke-free for the next 12 hours")
                    }
                }
            }

            /* weekly goals list (if any) */
            if (weeklyGoals.isNotEmpty()) {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Goals for this week:", style = MaterialTheme.typography.titleMedium)
                        weeklyGoals.forEach { Text("• $it") }
                    }
                }
            }

            /* mood picker */
            Text("How are you feeling this morning?")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                emojiSet.forEach { (idx, icon) ->
                    IconToggleButton(
                        checked = mood == idx,
                        onCheckedChange = { mood = idx },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            icon, null,
                            tint = if (mood == idx) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current
                        )
                    }
                }
            }

            /* reflection prompts */
            OutlinedTextField(
                value = whyKeep,
                onValueChange = { whyKeep = it },
                label = { Text("What keeps you going toward today’s goal?") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = whatMatters,
                onValueChange = { whatMatters = it },
                label = { Text("What matters most to you right now?") },
                modifier = Modifier.fillMaxWidth()
            )

            /* optional note */
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Anything else on your mind?") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
