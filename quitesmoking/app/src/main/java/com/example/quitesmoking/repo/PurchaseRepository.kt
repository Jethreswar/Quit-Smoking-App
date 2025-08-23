package com.example.quitesmoking.repo

import com.example.quitesmoking.model.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Very small helper object – no caching, just thin wrappers around Firestore.
 */
object PurchaseRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Push one new Purchase document under users/{uid}/purchases. */
    suspend fun add(p: Purchase) =
        db.collection("users").document(uid)
            .collection("purchases")
            .add(p)
            .await()

    /** Listen for live changes and feed the full list back to UI. */
    suspend fun listen(onChange: (List<Purchase>) -> Unit) =
        db.collection("users").document(uid)
            .collection("purchases")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    onChange(snap.documents.mapNotNull { it.toObject(Purchase::class.java) })
                }
            }
}
