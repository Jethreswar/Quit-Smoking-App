package com.example.quitesmoking.urge

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class VideoDoc(
    val title: String = "",
    val url: String = "",
    val thumbnail: String = ""
)

class MindfulnessRepoFs {
    private val col = FirebaseFirestore.getInstance().collection("resources")

    // Coroutine version (uses kotlinx-coroutines-play-services)
    suspend fun fetchAll(): List<VideoDoc> {
        val snap = col.get().await()
        return snap.documents
            .mapNotNull { it.toObject(VideoDoc::class.java) }
            .filter { it.url.isNotBlank() }
    }
}
