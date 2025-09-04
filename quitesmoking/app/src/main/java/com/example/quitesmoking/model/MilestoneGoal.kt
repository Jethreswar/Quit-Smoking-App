package com.example.quitesmoking.model

import java.util.Date

/**
 * Milestone goal data for tracking smoke-free days and e-cig usage limits.
 *
 * Stored at: users/{uid}/milestoneGoals/{goalId}
 */
data class MilestoneGoal(
    val smokeFreeTarget: Int = 0,           // Target number of smoke-free days
    val maxUsageTarget: Int = 0,            // Maximum allowed e-cig uses
    val goalPeriod: String = "This Week",   // Goal period (This Week, This Month, All Time)
    val startDate: Date = Date(),           // Start date of the goal period
    val endDate: Date = Date(),             // End date of the goal period
    val createdAt: Date = Date(),           // When the goal was created
    val isActive: Boolean = true,           // Whether this goal is currently active
    val totalSmokeFreeDays: Int = 0,        // Total smoke-free days achieved so far
    val lastUpdated: Date = Date()          // When the goal was last updated
)
