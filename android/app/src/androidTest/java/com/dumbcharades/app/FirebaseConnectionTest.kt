package com.dumbcharades.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Opt-in live check. Uses disposable anonymous identities and removes test data. */
@RunWith(AndroidJUnit4::class)
class FirebaseConnectionTest {
    @Test fun liveFirebaseAuthenticationAndPrivateBackups() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyFirebaseIntegration") == "true")
        withTimeout(120_000) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val apps = List(3) {
                FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, "device-check-${UUID.randomUUID()}")
            }
            val ownerAuth = FirebaseAuth.getInstance(apps[0])
            val strangerAuth = FirebaseAuth.getInstance(apps[1])
            val stores = apps.map { FirebaseFirestore.getInstance(it).apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
            } }
            val db = stores[0]
            val paths = mutableListOf<String>()
            try {
                val owner = ownerAuth.signInAnonymously().await().user!!
                assertTrue(owner.isAnonymous)
                assertEquals(owner.uid, ownerAuth.signInAnonymously().await().user!!.uid)
                for (kind in listOf("custom_decks", "game_sessions")) {
                    val id = UUID.randomUUID().toString()
                    val path = "players/${owner.uid}/$kind/$id"
                    paths += path
                    db.document(path).set(mapOf("id" to id, "payload" to "{}", "updatedAt" to FieldValue.serverTimestamp())).await()
                    assertEquals("{}", db.document(path).get(Source.SERVER).await().getString("payload"))
                }
                val stranger = strangerAuth.signInAnonymously().await().user!!
                assertTrue(stranger.isAnonymous && stranger.uid != owner.uid)
                for (path in paths) {
                    val readError = runCatching { stores[1].document(path).get(Source.SERVER).await() }.exceptionOrNull()
                    assertTrue(readError is FirebaseFirestoreException && readError.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                    val writeError = runCatching { stores[1].document(path).update("payload", "cross-user").await() }.exceptionOrNull()
                    assertTrue(writeError is FirebaseFirestoreException && writeError.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                }
                val publicError = runCatching { stores[2].document(paths.first()).get(Source.SERVER).await() }.exceptionOrNull()
                assertTrue(publicError is FirebaseFirestoreException && publicError.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
            } finally {
                withContext(NonCancellable) {
                    withTimeout(30_000) {
                        for (path in paths) db.document(path).delete().await()
                        strangerAuth.currentUser?.delete()?.await()
                        ownerAuth.currentUser?.delete()?.await()
                        for (store in stores) store.terminate().await()
                        for (app in apps) app.delete()
                    }
                }
            }
        }
    }
}
