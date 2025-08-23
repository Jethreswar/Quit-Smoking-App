package com.example.quitesmoking.model

/**
 * A lightweight weekly goal plan.
 *
 * Stored at: users/{uid}/weeklyGoals/{yyyy-'W'ww} (ISO-week key)
 */
data class WeeklyGoal(
    val targets: List<String> = emptyList(),   // ✅ text goals
    val reward: String = ""                    // e.g. “Go to the movies”
)
