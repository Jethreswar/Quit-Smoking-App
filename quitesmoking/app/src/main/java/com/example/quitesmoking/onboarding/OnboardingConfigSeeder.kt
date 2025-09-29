package com.example.quitesmoking.onboarding

import android.content.Context
import com.example.quitesmoking.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object OnboardingConfigSeeder {

    private const val COLLECTION = "configs"
    private const val DOC_ID = "onboarding_v1"

    private fun loadJsonAsMap(context: Context, rawResId: Int): Map<String, Any> {
        val input = context.resources.openRawResource(rawResId)
        InputStreamReader(input).use { reader ->
            val type = object : TypeToken<Map<String, Any>>() {}.type
            @Suppress("UNCHECKED_CAST")
            return Gson().fromJson<Map<String, Any>>(reader, type) as Map<String, Any>
        }
    }

    /**
     * Upsert the entire JSON into configs/onboarding_v1.
     * Safe to run multiple times; uses merge().
     */
    fun seedOrUpdate(context: Context, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val data = loadJsonAsMap(context, R.raw.onboarding)
        FirebaseFirestore.getInstance()
            .collection(COLLECTION)
            .document(DOC_ID)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}