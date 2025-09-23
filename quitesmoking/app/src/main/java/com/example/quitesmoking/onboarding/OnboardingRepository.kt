package com.example.quitesmoking.onboarding

import com.example.quitesmoking.model.AnswerBag
import com.example.quitesmoking.model.OnboardingConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Firestore Onboarding repo
 * - User doc:          /users/{uid}
 * - Aggregate doc:     /users/{uid}/onboardingResponses/onboarding
 * - Per-question docs: /users/{uid}/onBoarding/{questionId}
 *
 * You can keep your current "save once at summary" flow (aggregate or per-question)
 * or enable live per-question writes via saveAnswerDoc(...).
 */
class OnboardingRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /* ---------------- refs ---------------- */

    private fun uid(): String =
        requireNotNull(auth.currentUser?.uid) { "No signed-in user" }

    private fun userDocRef(): DocumentReference =
        db.collection("users").document(uid())

    // Aggregate (legacy/optional)
    private fun obDocRef(): DocumentReference =
        userDocRef().collection("onboardingResponses").document("onboarding")

    // Per-question collection (note: camelCase 'onBoarding' per your spec)
    private fun onBoardingColl() = userDocRef().collection("onBoarding")
    private fun questionDoc(qid: String) = onBoardingColl().document(qid)

    /* ---------------- utils ---------------- */

    private fun sanitizeDeep(v: Any?): Any? = when (v) {
        null -> null
        is String, is Number, is Boolean -> v
        is List<*> -> v.map { sanitizeDeep(it) }
        is Map<*, *> -> v.mapKeys { it.key.toString() }.mapValues { sanitizeDeep(it.value) }
        else -> v.toString()
    }

/* ---------------- API: load/save answers map (aggregate) ---------------- */



suspend fun loadAnswers(): AnswerBag {
    run{
        val snap = obDocRef().get().await()
        @Suppress("UNCHECKED_CAST")
        val map = (snap.get("answers") as? Map<String, Any?>)
        if (map != null) return map.toMutableMap()
    }

// Fallback: build map from per-question docs
        val list = onBoardingColl().get().await().documents
        val reconstructed = mutableMapOf<String, Any?>()
        list.forEach { d ->
        val qid = d.getString("questionId") ?: d.id
        reconstructed[qid] = d.get("answer")
        }
        return reconstructed
    }

/**
 * (Legacy optional) Save one answer into the aggregate answers map.
 * Keep if you still want autosave (aggregate) during the flow.
*/
suspend fun saveAnswer(id: String, value: Any?, deleteWhenNull: Boolean = false) {
    val obRef = obDocRef()
        if (deleteWhenNull && value == null) {
        db.runTransaction { tx ->
        tx.set(
        obRef,
        mapOf(
        "answers.$id" to FieldValue.delete(),
        "updatedAt" to FieldValue.serverTimestamp()
        ),
        SetOptions.merge()
        )
        }.await()
        return
        }
        db.runTransaction { tx ->
        tx.set(
        obRef,
        mapOf(
        "answers.$id" to sanitizeDeep(value),
        "updatedAt" to FieldValue.serverTimestamp()
        ),
        SetOptions.merge()
        )
        }.await()
    }

/** (Legacy optional) Save a batch into the aggregate answers map. */
suspend fun saveAnswers(batch: Map<String, Any?>) {
val obRef = obDocRef()
val flat = batch
.mapValues { sanitizeDeep(it.value) }
.mapKeys { (k, _) -> "answers.$k" }
.toMutableMap()
flat["updatedAt"] = FieldValue.serverTimestamp()

db.runTransaction { tx ->
tx.set(obRef, flat, SetOptions.merge())
}.await()
}

/* ---------------- API: per-question docs ---------------- */

/** Save/merge ONE question document now (live-per-question saving). */
suspend fun saveAnswerDoc(
questionId: String,
questionLabel: String,
answer: Any?,
version: Int? = null
) {
val user = requireNotNull(auth.currentUser) { "No signed-in user" }
val data = mutableMapOf<String, Any?>(
"questionId" to questionId,
"question" to questionLabel,
"answer" to sanitizeDeep(answer),
"userId" to user.uid,
"userName" to (user.displayName ?: user.email ?: ""),
"answeredAt" to FieldValue.serverTimestamp()
)
if (version != null) data["version"] = version
questionDoc(questionId).set(data, SetOptions.merge()).await()
}

/**
 * Batch-write ALL answers as individual docs under /onBoarding/{questionId},
 * then set completion flags on the user doc.
*/
suspend fun finalizeWithPerQuestionDocs(
    cfg: OnboardingConfig,
        answers: Map<String, Any?>,
            version: Int? = cfg.version
            ) {
    val userRef = userDocRef()
    val batch = db.batch()

    answers.forEach { (qid, raw) ->
        val qLabel = cfg.questionMap[qid]?.question ?: qid
        val ref = questionDoc(qid)
        val data = mutableMapOf<String, Any?>(
        "questionId" to qid,
        "question" to qLabel,
        "answer" to sanitizeDeep(raw),
        "userId" to uid(),
        "userName" to (auth.currentUser?.displayName ?: auth.currentUser?.email ?: ""),
        "answeredAt" to FieldValue.serverTimestamp()
    )
        if (version != null) data["version"] = version
        batch.set(ref, data, SetOptions.merge())
    }

    batch.set(
        userRef,
        mapOf(
        "completedOnboarding" to true,
        "onboardingCompletionDate" to FieldValue.serverTimestamp()
    ),
    SetOptions.merge()
)

batch.commit().await()
}

/* ---------------- API: finalize (aggregate snapshot) ---------------- */

/**
 * Single-write finalize used by the Summary screen (aggregate snapshot):
 * - users/{uid}:
 *     completedOnboarding = true
 *     onboardingCompletionDate = server timestamp
 *     onboardingResponse = full sanitized snapshot
 * - users/{uid}/onboardingResponses/onboarding:
 *     completed = true
 *     answersSnapshot = full sanitized snapshot
*/
suspend fun finalizeWithResponse(answers: Map<String, Any?>) {
        val userRef = userDocRef()
        val obRef = obDocRef()
        val snapshot = answers.mapValues { sanitizeDeep(it.value) }

        val batch = db.batch()
        batch.set(
        userRef,
        mapOf(
        "completedOnboarding" to true,
        "onboardingCompletionDate" to FieldValue.serverTimestamp(),
        "onboardingResponse" to snapshot
    ),
    SetOptions.merge()
    )
    batch.set(
        obRef,
        mapOf(
        "completed" to true,
        "answersSnapshot" to snapshot,
        "updatedAt" to FieldValue.serverTimestamp()
    ),
        SetOptions.merge()
    )
        batch.commit().await()
    }

/* ---------------- misc ---------------- */

suspend fun markComplete() {
    val userRef = userDocRef()
    val obRef = obDocRef()
    val batch = db.batch()
    batch.set(
        userRef,
        mapOf(
        "completedOnboarding" to true,
        "onboardingCompletionDate" to FieldValue.serverTimestamp()
    ),
        SetOptions.merge()
    )
    batch.set(
        obRef,
        mapOf("completed" to true, "updatedAt" to FieldValue.serverTimestamp()),
        SetOptions.merge()
    )
    batch.commit().await()
}

suspend fun isComplete(): Boolean {
    val root = userDocRef().get().await()
    return root.getBoolean("completedOnboarding") == true
}

suspend fun resetAll() {
// clear flags + aggregate doc fields
        val userRef = userDocRef()
        val obRef = obDocRef()
        val batch = db.batch()
    batch.set(
        userRef,
        mapOf(
        "completedOnboarding" to false,
        "onboardingResponse" to FieldValue.delete(),
        "onboardingCompletionDate" to FieldValue.delete()
    ),
        SetOptions.merge()
    )
    batch.set(
        obRef,
        mapOf(
        "answers" to FieldValue.delete(),
        "answersSnapshot" to FieldValue.delete(),
        "completed" to false,
        "updatedAt" to FieldValue.serverTimestamp()
    ),
        SetOptions.merge()
    )
    batch.commit().await()

// delete per-question docs
        val qs = onBoardingColl().get().await()
        if (!qs.isEmpty) {
            val delBatch = db.batch()
            qs.documents.forEach { delBatch.delete(it.reference) }
            delBatch.commit().await()
        }
    }
}