package com.example.quitesmoking.repo

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.quitesmoking.model.DailyLog
import com.example.quitesmoking.model.WeeklyGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

object DailyLogRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /* date helpers */
    @RequiresApi(Build.VERSION_CODES.O)
    private val df = DateTimeFormatter.ISO_DATE
    @RequiresApi(Build.VERSION_CODES.O)
    private fun key(date: LocalDate) = df.format(date)

    /* ──────────────────── Daily Log ──────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveMorning(
        date: LocalDate,
        mood: Int,
        note: String?,
    ) {
        db.collection("users").document(uid)
            .collection("dailyLogs").document(key(date))
            .set(mapOf("moodAM" to mood, "moodAMNote" to note))
            .await()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveNight(
        date: LocalDate,
        mood: Int,
        note: String?,
        cigs: Int,
        triggers: List<String>,
    ) {
        db.collection("users").document(uid)
            .collection("dailyLogs").document(key(date))
            .set(
                mapOf(
                    "moodPM" to mood,
                    "moodPMNote" to note,
                    "cigsUsed" to cigs,
                    "triggers" to triggers
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    /* ──────────────────── Weekly Goal ─────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun weekKey(of: LocalDate): String {
        val wf = WeekFields.of(Locale.US)
        val week = of.get(wf.weekOfWeekBasedYear())
        return "${of.year}-W${"%02d".format(week)}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveWeeklyGoal(startOfWeek: LocalDate, goal: WeeklyGoal) =
        db.collection("users").document(uid)
            .collection("weeklyGoals")
            .document(weekKey(startOfWeek))
            .set(goal)
            .await()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getWeeklyGoal(startOfWeek: LocalDate): WeeklyGoal? =
        db.collection("users").document(uid)
            .collection("weeklyGoals")
            .document(weekKey(startOfWeek))
            .get().await()
            .toObject(WeeklyGoal::class.java)
}
