package com.example.quitesmoking.onboarding

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quitesmoking.model.QuestionDef
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel,
    onCompleted: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    BackHandler(enabled = true) { /* block if desired */ }

    if (ui.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val cfg = ui.config ?: run {
        ErrorState(ui.error ?: "Could not load onboarding config.")
        return
    }

    // ---- One-shot navigate when the route ends ----
    var navigated by rememberSaveable { mutableStateOf(false) }
    val currentId = ui.currentId
    if (currentId == null) {
        LaunchedEffect(navigated) {
            if (!navigated) {
                navigated = true
                onCompleted() // go to Summary exactly once
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Pull the question for this id (don’t use as AnimatedContent target directly)
    val q: QuestionDef? = remember(currentId) { cfg.questionMap[currentId] }
    if (q == null) {
        // Unknown id → skip forward once
        LaunchedEffect(currentId) { vm.goNextFrom(currentId, onCompleted) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Local working value bound to this question id
    var localValue by remember(currentId) { mutableStateOf(ui.answers[currentId]) }

    // Decide slide direction
    var lastId by remember { mutableStateOf(currentId) }
    var slideLeft by remember { mutableStateOf(true) }
    LaunchedEffect(currentId) {
        slideLeft = shouldSlideLeft(lastId, currentId)
        lastId = currentId
    }

    // Optional: debounce Next so double-tap can’t cause flicker
    var isAdvancing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Onboarding") }) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val scope = rememberCoroutineScope()
                    Button(
                        enabled = !isAdvancing,
                        onClick = {
                            scope.launch {
                                isAdvancing = true
                                // ✅ in-memory only; do NOT persist yet
                                vm.setLocalAnswer(currentId, localValue)
                                vm.goNextFrom(currentId) { onCompleted() }
                                isAdvancing = false
                            }
                        }
                    ) { Text("Next") }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Best-effort progress (branching makes it approximate)
            LinearProgressIndicator(
                progress = estimateProgress(cfg, ui.answers, currentId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ❗ Use currentId (String) as the targetState to avoid flicker
            AnimatedContent(
                targetState = currentId,
                transitionSpec = {
                    val duration = 200
                    if (slideLeft) {
                        (slideInHorizontally(
                            initialOffsetX = { full ->  full },  // enter from right
                            animationSpec = tween(duration)
                        ) + fadeIn(tween(duration)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { full -> -full }, // exit to left
                                    animationSpec = tween(duration)
                                ) + fadeOut(tween(duration))
                            )
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { full -> -full },  // enter from left
                            animationSpec = tween(duration)
                        ) + fadeIn(tween(duration)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { full ->  full }, // exit to right
                                    animationSpec = tween(duration)
                                ) + fadeOut(tween(duration))
                            )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                label = "onboarding-slide"
            ) { id ->
                val question = remember(id) { cfg.questionMap[id] }
                if (question == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    QuestionCard(
                        id = id,
                        def = question,
                        value = localValue,
                        onValueChange = { localValue = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    id: String,
    def: QuestionDef,
    value: Any?,
    onValueChange: (Any?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(def.question, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            when (def.type) {
                "textInput" -> TextInputField(value as? String ?: "", onValueChange)
                "singleChoice" -> SingleChoiceList(
                    options = def.options.orEmpty(),
                    selected = value as? String,
                    onSelect = onValueChange
                )
                "multiChoice" -> MultiChoiceList(
                    options = def.options.orEmpty(),
                    selected = (value as? List<*>)?.map { it?.toString().orEmpty() }?.toSet() ?: emptySet(),
                    onToggle = { opt, checked ->
                        val curr = (value as? List<*>)?.map { it?.toString().orEmpty() }?.toMutableList() ?: mutableListOf()
                        if (checked) {
                            if (!curr.contains(opt)) curr.add(opt)
                        } else {
                            curr.remove(opt)
                        }
                        onValueChange(curr.toList())
                    }
                )
                "datePicker" -> DatePickerField(
                    current = value as? String,
                    onPicked = onValueChange
                )
                "imageCapture" -> ImagePickerField(
                    current = value as? String,
                    onPicked = onValueChange
                )
                else -> Text("Unsupported type: ${def.type}")
            }
        }
    }
}

/* ---------- Controls ---------- */

@Composable
private fun TextInputField(
    text: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun SingleChoiceList(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            ElevatedCard(
                onClick = { onSelect(opt) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == opt,
                        onClick = { onSelect(opt) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(opt, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun MultiChoiceList(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val checked = selected.contains(opt)
            ElevatedCard(
                onClick = { onToggle(opt, !checked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { onToggle(opt, it) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(opt, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    current: String?,
    onPicked: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val label = current ?: "Pick a date"

    OutlinedButton(onClick = { open = true }) {
        Text(label)
    }

    if (open) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) onPicked(formatYyyyMmDd(millis))
                    open = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun ImagePickerField(
    current: String?,
    onPicked: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onPicked(uri.toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { launcher.launch("image/*") }) {
            Text(if (current == null) "Choose photo" else "Change photo")
        }
        if (current != null) {
            Text("Selected: $current", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* ---------- Helpers ---------- */

private fun estimateProgress(
    cfg: com.example.quitesmoking.model.OnboardingConfig,
    answers: Map<String, Any?>,
    currentId: String
): Float {
    val total = cfg.questionMap.size.coerceAtLeast(1)
    val answered = answers.keys.intersect(cfg.questionMap.keys).size
    val base = answered.toFloat() / total
    return (base + (1f / total)).coerceIn(0f, 1f)
}

private fun shouldSlideLeft(prevId: String?, currId: String?): Boolean {
    if (prevId == null || currId == null) return true
    return currId >= prevId
}

private fun formatYyyyMmDd(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date(millis))
}

@Composable
private fun ErrorState(msg: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: $msg")
    }
}

@Composable
private fun DoneState(onDone: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("All set!")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDone) { Text("Continue") }
        }
    }
}
