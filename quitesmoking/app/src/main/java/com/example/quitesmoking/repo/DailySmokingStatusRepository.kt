package com.example.quitesmoking.repo

import com.example.quitesmoking.model.DailySmokingStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Repository for managing daily smoking status data.
 */
object DailySmokingStatusRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    // ---- Date helpers ----
    private fun startOfDay(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun endOfDay(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    private fun monthRange(year: Int, monthZeroBased: Int): Pair<Date, Date> {
        val cal = Calendar.getInstance(Locale.getDefault())

        // Start
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time

        // End
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.time

        return start to end
    }

    private fun weekRangeContaining(date: Date, weekStartsOn: Int = Calendar.SUNDAY): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.firstDayOfWeek = weekStartsOn

        // Start of week
        cal.set(Calendar.DAY_OF_WEEK, weekStartsOn)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time

        // End of week
        cal.add(Calendar.DATE, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.time

        return start to end
    }
    // ----------------------

    /**
     * Get smoking status for a specific date range.
     */
    suspend fun getSmokingStatusForRange(startDate: Date, endDate: Date): List<DailySmokingStatus> {
        if (uid.isEmpty()) return emptyList()

        val snapshot = db.collection("users").document(uid)
            .collection("dailyCheckResponses")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val timestamp = doc.getTimestamp("timestamp")?.toDate()
            val answer = doc.getString("answer") ?: ""
            val notes = doc.getString("notes")
            val mood = doc.getLong("mood")?.toInt()
            val triggers = (doc.get("triggers") as? List<*>)?.filterIsInstance<String>().orEmpty()

            timestamp?.let {
                DailySmokingStatus(
                    date = it,
                    isSmokeFree = answer.lowercase(Locale.getDefault()).contains("no") || answer == "否",
                    answer = answer,
                    timestamp = it,
                    notes = notes,
                    mood = mood,
                    triggers = triggers
                )
            }
        }.sortedBy { it.date }
    }

    /**
     * Get total smoke-free days for all time.
     */
    suspend fun getTotalSmokeFreeDays(): Int {
        if (uid.isEmpty()) return 0

        val snapshot = db.collection("users").document(uid)
            .collection("dailyCheckResponses")
            .get()
            .await()

        return snapshot.documents.count { doc ->
            val answer = doc.getString("answer") ?: ""
            answer.lowercase(Locale.getDefault()).contains("no") || answer == "否"
        }
    }

    /**
     * Get smoke-free days for a specific month.
     * @param month Zero-based (Jan = 0, Dec = 11).
     */
    suspend fun getSmokeFreeDaysForMonth(year: Int, month: Int): List<DailySmokingStatus> {
        val (startDate, endDate) = monthRange(year, month)
        return getSmokingStatusForRange(startDate, endDate)
    }

    /**
     * Get smoke-free days for a specific week (week containing startDate).
     */
    suspend fun getSmokeFreeDaysForWeek(startDate: Date): List<DailySmokingStatus> {
        val (weekStart, weekEnd) = weekRangeContaining(startDate, Calendar.SUNDAY)
        return getSmokingStatusForRange(weekStart, weekEnd)
    }

    /**
     * Calculate streak of consecutive smoke-free days.
     */
    suspend fun getCurrentStreak(): Int {
        if (uid.isEmpty()) return 0

        val allStatuses = db.collection("users").document(uid)
            .collection("dailyCheckResponses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val sortedStatuses = allStatuses.documents.mapNotNull { doc ->
            val timestamp = doc.getTimestamp("timestamp")?.toDate()
            val answer = doc.getString("answer") ?: ""
            timestamp?.let {
                DailySmokingStatus(
                    date = it,
                    isSmokeFree = answer.lowercase(Locale.getDefault()).contains("no") || answer == "否",
                    answer = answer,
                    timestamp = it
                )
            }
        }.sortedByDescending { it.date }

        var streak = 0
        val todayStart = startOfDay(Date())

        for (status in sortedStatuses) {
            if (!status.isSmokeFree) break

            val statusStart = startOfDay(status.date)
            val daysDiff = ((todayStart.time - statusStart.time) / (1000L * 60 * 60 * 24)).toInt()

            if (daysDiff == streak) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Get smoking status for a specific date.
     */
    suspend fun getSmokingStatusForDate(date: Date): DailySmokingStatus? {
        if (uid.isEmpty()) return null

        val start = startOfDay(date)
        val end = endOfDay(date)

        val snapshot = db.collection("users").document(uid)
            .collection("dailyCheckResponses")
            .whereGreaterThanOrEqualTo("timestamp", start)
            .whereLessThanOrEqualTo("timestamp", end)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.let { doc ->
            val timestamp = doc.getTimestamp("timestamp")?.toDate()
            val answer = doc.getString("answer") ?: ""
            val notes = doc.getString("notes")
            val mood = doc.getLong("mood")?.toInt()
            val triggers = (doc.get("triggers") as? List<*>)?.filterIsInstance<String>().orEmpty()

            timestamp?.let {
                DailySmokingStatus(
                    date = it,
                    isSmokeFree = answer.lowercase(Locale.getDefault()).contains("no") || answer == "否",
                    answer = answer,
                    timestamp = it,
                    notes = notes,
                    mood = mood,
                    triggers = triggers
                )
            }
        }
    }
}