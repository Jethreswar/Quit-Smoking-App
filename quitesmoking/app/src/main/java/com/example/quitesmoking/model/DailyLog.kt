package com.example.quitesmoking.model

/**
 * One calendar day’s smoking & mood record.
 *
 * Stored at: users/{uid}/dailyLogs/{yyyy-MM-dd}
 */
data class DailyLog(
    val moodAM: Int? = null,      // 1-5 emoji index, higher = better
    val moodAMNote: String? = null,
    val moodPM: Int? = null,
    val moodPMNote: String? = null,
    val cigsUsed: Int? = null,
    val triggers: List<String> = emptyList()   // e.g. ["Social", "Stress"]
)
