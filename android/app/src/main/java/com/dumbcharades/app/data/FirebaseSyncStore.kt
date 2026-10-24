package com.dumbcharades.app.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/** Private native snapshots; no separate API server is needed for cloud backup. */
@Singleton
class FirebaseSyncStore @Inject constructor(private val firestore: FirebaseFirestore) {
    private fun collection(uid: String, kind: String) =
        firestore.collection("players").document(uid).collection(kind)

    suspend fun upload(uid: String, kind: String, id: String, payload: String) = withTimeout(20_000) {
        require(uid.isNotBlank())
        require(kind == DECKS || kind == SESSIONS)
        require(payload.toByteArray(Charsets.UTF_8).size <= if (kind == DECKS) 200_000 else 500_000) { "This backup is too large to sync" }
        collection(uid, kind).document(id).set(mapOf(
            "id" to id, "payload" to payload, "updatedAt" to FieldValue.serverTimestamp(),
        )).await()
    }

    suspend fun download(uid: String, kind: String): List<Pair<String, String>> = withTimeout(20_000) {
        require(uid.isNotBlank())
        collection(uid, kind).get(Source.SERVER).await().documents.mapNotNull { doc ->
            doc.getString("payload")?.let { doc.id to it }
        }
    }

    companion object {
        const val DECKS = "custom_decks"
        const val SESSIONS = "game_sessions"
    }
}
