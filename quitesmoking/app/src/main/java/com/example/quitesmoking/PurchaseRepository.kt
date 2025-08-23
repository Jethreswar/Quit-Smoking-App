package com.example.quitesmoking

import com.example.quitesmoking.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PurchaseRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun add(p: Purchase) =
        db.collection("users").document(uid)
            .collection("purchases")
            .add(p)
            .await()

    suspend fun listen(onChange: (List<Purchase>) -> Unit) =
        db.collection("users").document(uid)
            .collection("purchases")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    onChange(snap.documents.mapNotNull { it.toObject(Purchase::class.java) })
                }
            }
}