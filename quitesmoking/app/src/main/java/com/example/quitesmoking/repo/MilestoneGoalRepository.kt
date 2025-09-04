package com.example.quitesmoking.repo

import com.example.quitesmoking.model.MilestoneGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repository for managing milestone goals in Firestore.
 */
object MilestoneGoalRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Save a milestone goal to Firestore.
     */
    suspend fun saveMilestoneGoal(goal: MilestoneGoal): String {
        val goalRef = db.collection("users").document(uid)
            .collection("milestoneGoals")
            .add(goal)
            .await()
        return goalRef.id
    }

    /**
     * Update an existing milestone goal.
     */
    suspend fun updateMilestoneGoal(goalId: String, goal: MilestoneGoal) {
        db.collection("users").document(uid)
            .collection("milestoneGoals")
            .document(goalId)
            .set(goal)
            .await()
    }

    /**
     * Get the current active milestone goal.
     */
    suspend fun getCurrentMilestoneGoal(): MilestoneGoal? {
        val snapshot = db.collection("users").document(uid)
            .collection("milestoneGoals")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        
        return snapshot.documents.firstOrNull()?.toObject(MilestoneGoal::class.java)
    }

    /**
     * Get all milestone goals for a user.
     */
    suspend fun getAllMilestoneGoals(): List<MilestoneGoal> {
        val snapshot = db.collection("users").document(uid)
            .collection("milestoneGoals")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { it.toObject(MilestoneGoal::class.java) }
    }

    /**
     * Deactivate all existing goals and activate a new one.
     */
    suspend fun activateNewGoal(newGoal: MilestoneGoal) {
        // First, deactivate all existing goals
        val existingGoals = db.collection("users").document(uid)
            .collection("milestoneGoals")
            .whereEqualTo("isActive", true)
            .get()
            .await()
        
        existingGoals.documents.forEach { doc ->
            doc.reference.update("isActive", false)
        }
        
        // Then save the new active goal
        saveMilestoneGoal(newGoal)
    }
}
