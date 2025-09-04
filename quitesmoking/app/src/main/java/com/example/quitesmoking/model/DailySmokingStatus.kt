package com.example.quitesmoking.model

import java.util.Date

/**
 * Daily smoking status for tracking smoke-free days across the calendar.
 *
 * Stored at: users/{uid}/dailySmokingStatus/{yyyy-MM-dd}
 */
data class DailySmokingStatus(
    val date: Date = Date(),                    // The date this status represents
    val isSmokeFree: Boolean = false,           // Whether the user was smoke-free on this day
    val answer: String = "",                    // Original answer from daily check-in
    val timestamp: Date = Date(),               // When this status was recorded
    val notes: String? = null,                  // Optional notes for the day
    val mood: Int? = null,                      // User's mood for the day (1-5)
    val triggers: List<String> = emptyList()    // Triggers that led to smoking (if any)
)
