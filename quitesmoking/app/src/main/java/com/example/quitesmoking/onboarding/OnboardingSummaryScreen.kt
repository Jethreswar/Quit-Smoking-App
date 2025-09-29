package com.example.quitesmoking.onboarding

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quitesmoking.model.OnboardingConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSummaryScreen(
    vm: OnboardingViewModel,
    onEdit: () -> Unit,
    onGoHome: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val cfg: OnboardingConfig = ui.config ?: return

    // One-time snapshot so the list stays stable while the user reviews.
    val answers = remember { vm.computeFinalSnapshot() }

    // Pretty JSON (printed to Logcat too).
    val pretty = remember(answers) { OnboardingJson.toJsonString(answers, pretty = true) }

    LaunchedEffect(pretty) {
        Log.d("ONBOARDING_SUMMARY", "\n$pretty")
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Onboarding Summary") }) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            // ✅ Single write to Firestore (or skip if testing local-only),
                            // then navigate home.
                            vm.finalizeAndMarkCompletePerQuestion(onDone = onGoHome)
                        }
                    ) { Text("Save & Continue to Home") }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Review your answers", style = MaterialTheme.typography.titleMedium)

            answers.entries.forEach { (id, value) ->
                val title = cfg.questionMap[id]?.question ?: id
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            TextButton(
                                onClick = {
                                    // Jump VM to this question, then navigate to the onboarding screen
                                    vm.jumpTo(id) // or vm.goBack(id)
                                    onEdit()
                                }
                            ) { Text("Edit") }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(value.toReadable(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/* ---------- Helpers ---------- */

private fun Any?.toReadable(): String = when (this) {
    null -> "—"
    is List<*> -> this.joinToString(", ") { it?.toString().orEmpty() }
    is Boolean, is Number, is String -> this.toString()
    is Map<*, *> -> this.entries.joinToString(", ") { (k, v) -> "${k.toString()}: ${v.toReadable()}" }
    else -> this.toString()
}
