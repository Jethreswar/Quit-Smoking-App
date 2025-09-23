package com.example.quitesmoking.onboarding

import android.content.Context
import androidx.annotation.RawRes
import com.example.quitesmoking.model.OnboardingConfig
import com.example.quitesmoking.utils.JsonLenient
import com.example.quitesmoking.utils.readRawText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class OnboardingConfigRepo(
    private val appContext: Context,
    private val db: FirebaseFirestore,
    @RawRes private val localResId: Int,                 // e.g. R.raw.onboarding
    private val firestoreDocPath: String = "configs/onboarding_v1", // your doc path
    private val preferLocalFirst: Boolean = true         // dev/test = true, prod can set false
) {
    private val json: Json = JsonLenient

    /** Public API: load config (local first, then remote). */
    suspend fun loadConfig(): OnboardingConfig {
        // 1) Local (always try first when testing)
        val local = runCatching { loadLocal() }.getOrNull()

        if (preferLocalFirst) {
            // try remote override, else keep local
            val remote = runCatching { loadRemote() }.getOrNull()
            return remote ?: requireNotNull(local) { "Local onboarding.json missing/invalid" }
        } else {
            // prod: try remote first, fallback to local
            val remote = runCatching { loadRemote() }.getOrNull()
            return remote ?: requireNotNull(local) { "Local onboarding.json missing/invalid" }
        }
    }

    /** Read and decode res/raw JSON exactly matching your schema. */
    private fun loadLocal(): OnboardingConfig {
        val raw = appContext.readRawText(localResId)
        return json.decodeFromString(OnboardingConfig.serializer(), raw)
    }

    /**
     * Fetch Firestore document at [firestoreDocPath] that contains the SAME JSON structure:
     * {
     *   "version": 1,
     *   "questionMap": { ... },
     *   "routing": { ... }
     * }
     */
    private suspend fun loadRemote(): OnboardingConfig? {
        val snap = db.document(firestoreDocPath).get().await()
        if (!snap.exists()) return null
        val data = snap.data ?: return null

        // Convert Firestore Map -> JsonElement recursively, then decode with Kotlinx.
        val rootEl = anyToJsonElement(data)
        return json.decodeFromJsonElement(OnboardingConfig.serializer(), rootEl)
    }

    /** Recursively convert Firestore Maps/Lists/Primitives into kotlinx JsonElement. */
    private fun anyToJsonElement(any: Any?): JsonElement = when (any) {
        null -> JsonNull
        is String -> JsonPrimitive(any)
        is Number -> JsonPrimitive(any)
        is Boolean -> JsonPrimitive(any)
        is Map<*, *> -> JsonObject(
            any.entries.associate { (k, v) -> k.toString() to anyToJsonElement(v) }
        )
        is List<*> -> JsonArray(any.map { anyToJsonElement(it) })
        else -> JsonPrimitive(any.toString()) // fallback
    }
}
