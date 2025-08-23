@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.example.quitesmoking.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quitesmoking.repo.DailyLogRepository
import com.example.quitesmoking.navigation.Routes
import kotlinx.coroutines.launch
import java.time.LocalDate

private val triggers = listOf("Stress", "Social", "Boredom", "Alcohol", "Habit")

@SuppressLint("NewApi")
@Composable
fun NightCheckInScreen(nav: NavController) {
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    var cigs by remember { mutableStateOf("0") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var mood by remember { mutableStateOf<Int?>(null) }
    var note by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Night review") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val m = mood ?: return@ExtendedFloatingActionButton
                    val cigCount = cigs.toIntOrNull() ?: 0
                    scope.launch {
                        try {
                            DailyLogRepository.saveNight(
                                LocalDate.now(),
                                m,
                                note.takeIf { it.isNotBlank() },
                                cigCount,
                                selected.toList()
                            )
                            snack.showSnackbar("Logged!")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = cigs,
                onValueChange = { if (it.all(Char::isDigit)) cigs = it },
                label = { Text("Cigarettes today") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("What triggered you today?")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                triggers.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            FilterChip(
                                selected = option in selected,
                                onClick = {
                                    selected =
                                        if (option in selected) selected - option
                                        else selected + option
                                },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }

            Text("Evening mood")
            MoodSelector(mood) { mood = it }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Reflection (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MoodSelector(sel: Int?, onSel: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in 1..5) {
            AssistChip(
                onClick = { onSel(i) },
                label = { Text(i.toString()) },
                leadingIcon = if (sel == i) {
                    { Icon(Icons.Default.Check, null) }
                } else null
            )
        }
    }
}
