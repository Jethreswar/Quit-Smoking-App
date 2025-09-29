package com.example.quitesmoking.onboarding

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Encapsulates the "Edit Onboarding" behavior so UI code stays clean.
 */
class EditOnboarding(
    private val repo: OnboardingRepository
) {
    fun action(
        navController: NavController,
        scope: CoroutineScope,
        ctx: Context
    ): () -> Unit {
        return {
            scope.launch {
                val completed = repo.isComplete()
                if (completed) {
                    // ✅ Completed → go to summary (user can tap Edit per row)
                    navController.navigate("onboarding_summary")
                } else {
                    // ❌ Not completed → check if docs exist
                    val answers = repo.loadAnswers()
                    if (answers.isEmpty()) {
                        Toast.makeText(
                            ctx,
                            "Please complete onboarding to continue.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    navController.navigate("onboarding_flow")
                }
            }
        }
    }
}
