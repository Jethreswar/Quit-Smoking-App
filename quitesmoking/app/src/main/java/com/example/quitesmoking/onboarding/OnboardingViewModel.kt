package com.example.quitesmoking.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quitesmoking.model.AnswerBag
import com.example.quitesmoking.model.OnboardingConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// router helper
import com.example.quitesmoking.onboarding.nextIdFor

/** UI state */
data class OnboardingUiState(
    val isLoading: Boolean = true,
    val config: OnboardingConfig? = null,
    val answers: AnswerBag = mutableMapOf(),   // in-memory working answers
    val currentId: String? = null,
    val error: String? = null
)

/**
 * ViewModel
 * - Loads config + any previously saved answers (resume support)
 * - During the flow: ONLY edits in-memory answers (no persistence)
 * - On Summary "Save": persists once (aggregate or per-question)
 */
class OnboardingViewModel(
    private val configRepo: OnboardingConfigRepo,
    private val answersRepo: OnboardingRepository, // Firestore impl (or swap with LocalJson for tests)
    private val startId: String = "1"
) : ViewModel() {

    private val _ui = MutableStateFlow(OnboardingUiState())
    val ui: StateFlow<OnboardingUiState> = _ui

    init {
        viewModelScope.launch {
            try {
                val cfg = configRepo.loadConfig()
                // Load prior answers ONLY to resume; still write once at Summary
                val saved = answersRepo.loadAnswers()
                val resumeAt = computeResumeId(cfg, saved, startId)

                _ui.value = OnboardingUiState(
                    isLoading = false,
                    config = cfg,
                    answers = saved,       // local working copy (mutated in-memory)
                    currentId = resumeAt
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(isLoading = false, error = t.message)
            }
        }
    }

    /* ---------------- In-memory during the flow ---------------- */

    /** Update one answer locally (no persistence yet). */
    fun setLocalAnswer(id: String, value: Any?) {
        val curr = _ui.value
        val newMap = curr.answers.toMutableMap().apply { this[id] = value }
        _ui.value = curr.copy(answers = newMap)
    }

    /**
     * Advance using router & current in-memory answers.
     * If routing returns null → we've reached the end; caller should navigate to Summary.
     */
    fun goNextFrom(currentId: String, onFinished: () -> Unit) = viewModelScope.launch {
        val cfg = _ui.value.config ?: return@launch
        val next = nextIdFor(currentId, cfg, _ui.value.answers)
        if (next == null) {
            onFinished() // do NOT persist here
        } else {
            _ui.value = _ui.value.copy(currentId = next)
        }
    }

    /** Jump to a specific question (used by Summary “Edit”). */
    fun jumpTo(id: String) {
        _ui.value = _ui.value.copy(currentId = id)
    }

    /** Optional back navigation if you keep a stack externally. */
    fun goBack(toId: String?) {
        _ui.value = _ui.value.copy(currentId = toId)
    }

    /** Hard reset to the first question (keeps answers unless cleared separately). */
    fun resetToStart() {
        _ui.value = _ui.value.copy(currentId = startId)
    }

    /**
     * Walk the route from startId using existing answers.
     * Stop at the first node without an answer, or when route ends.
     */
    private fun computeResumeId(
        cfg: OnboardingConfig,
        answers: AnswerBag,
        start: String,
        maxHops: Int = 512
    ): String {
        var curr = start
        var hops = 0
        while (hops++ < maxHops) {
            if (!answers.containsKey(curr)) return curr
            val next = nextIdFor(curr, cfg, answers) ?: return curr // end → show last to allow edits
            if (!answers.containsKey(next)) return next
            curr = next
        }
        return start
    }

    /* ---------------- Finalize at Summary (single write) ---------------- */

    /** Immutable snapshot of all answers for preview/printing. */
    fun computeFinalSnapshot(): Map<String, Any?> = _ui.value.answers.toMap()

    /**
     * Persist once on Summary confirm (aggregate snapshot path).
     * - completedOnboarding = true
     * - onboardingCompletionDate = server ts
     * - onboardingResponse = full answers snapshot
     */
    fun finalizeAndMarkComplete(onDone: () -> Unit) = viewModelScope.launch {
        val snap = computeFinalSnapshot()
        answersRepo.finalizeWithResponse(snap)
        onDone()
    }

    /**
     * Persist once on Summary confirm (per-question docs under /onBoarding/{questionId})
     * + completion flags on /users/{uid}.
     */
    fun finalizeAndMarkCompletePerQuestion(onDone: () -> Unit) = viewModelScope.launch {
        val state = _ui.value
        val cfg = state.config ?: return@launch
        val snap = state.answers.toMap()
        answersRepo.finalizeWithPerQuestionDocs(cfg, snap)
        onDone()
    }

    /** Optional: live-per-question save if you want to persist on each "Next". */
    fun saveAnswerDoc(
        questionId: String,
        questionLabel: String,
        answer: Any?,
        version: Int? = 1
    ) = viewModelScope.launch {
        answersRepo.saveAnswerDoc(
            questionId = questionId,
            questionLabel = questionLabel,
            answer = answer,
            version = version
        )
    }
    /** Optional: clear everything (helpful for QA). */
    fun resetAllPersisted(onDone: (() -> Unit)? = null) = viewModelScope.launch {
        answersRepo.resetAll()
        _ui.value = OnboardingUiState(
            isLoading = false,
            config = _ui.value.config,
            answers = mutableMapOf(),
            currentId = startId
        )
        onDone?.invoke()
    }

    fun finalizeAndMarkCompletePerQuestion(
        onDone: () -> Unit,
        version: Int? = 1
    ) = viewModelScope.launch {
        val cfg = _ui.value.config ?: run { onDone(); return@launch }
        val snap = computeFinalSnapshot()
        answersRepo.finalizeWithPerQuestionDocs(
            cfg = cfg,
            answers = snap,
            version = version
        )
        onDone()
    }
}
