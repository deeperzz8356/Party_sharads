package com.dumbcharades.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dumbcharades.app.data.local.AppDatabase
import com.dumbcharades.app.data.local.DeckEntity
import com.dumbcharades.app.data.local.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupIsolationTest {
    @Test fun localBackupsStayWithTheirOwnerAndNewerWritesStayPending() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = database.gameDao()
            fun deck(id: String, owner: String, custom: Boolean = true) = DeckEntity(
                id, id, "", 1, "2026-10-07T00:00:00Z", 1, false, null, custom, false, "{}", custom, owner,
            )
            dao.upsertDecks(listOf(deck("builtin", "", false), deck("guest", ""), deck("a", "owner-a"), deck("b", "owner-b")))
            assertEquals(setOf("builtin", "guest"), dao.observeDecks("").first().map { it.id }.toSet())
            assertEquals(setOf("builtin", "a"), dao.observeDecks("owner-a").first().map { it.id }.toSet())
            assertEquals(listOf("a"), dao.pendingDecks("owner-a").map { it.id })
            val old = dao.deck("a")!!
            dao.upsertDecks(listOf(old.copy(payloadJson = "new")))
            dao.markDeckSynced(old.id, "owner-a", old.payloadJson)
            assertEquals("new", dao.pendingDecks("owner-a").single().payloadJson)
            dao.mergeRemoteDeck(old.copy(syncPending = false))
            assertEquals("new", dao.deck("a")!!.payloadJson)

            val session = SessionEntity("session", "old", false, "2026-10-07T00:00:00Z", true, "owner-a")
            dao.upsertSession(session)
            assertNull(dao.unfinishedSession("owner-b"))
            assertNull(dao.unfinishedSession(""))
            dao.upsertSession(session.copy(payloadJson = "new"))
            dao.markSessionSynced(session.id, "owner-a", "old")
            assertEquals("new", dao.pendingSessions("owner-a").single().payloadJson)
            dao.mergeRemoteSession(session.copy(syncPending = false, updatedAt = "2026-10-08T00:00:00Z"))
            assertEquals("new", dao.session(session.id)!!.payloadJson)
            dao.markSessionSynced(session.id, "owner-a", "new")
            assertEquals(emptyList<SessionEntity>(), dao.pendingSessions("owner-a"))
            dao.upsertSession(SessionEntity("guest-session", "guest", false, "2026-10-07T00:00:00Z", false))
            dao.adoptGuestData("device")
            assertEquals("device", dao.deck("guest")!!.ownerId)
            assertEquals("", dao.deck("builtin")!!.ownerId)
            assertEquals("owner-a", dao.deck("a")!!.ownerId)
            assertEquals("owner-b", dao.deck("b")!!.ownerId)
            assertEquals(listOf("guest-session"), dao.pendingSessions("device").map { it.id })
            assertEquals("owner-a", dao.session("session")!!.ownerId)
            dao.upsertSession(SessionEntity("finished-a", "{}", true, "2026-10-09T00:00:00Z", false, "owner-a"))
            dao.upsertSession(SessionEntity("finished-b", "{}", true, "2026-10-10T00:00:00Z", false, "owner-b"))
            assertEquals(listOf("finished-a", "session"), dao.sessionHistory("owner-a").first().map { it.id })
            assertEquals(listOf("finished-b"), dao.sessionHistory("owner-b").first().map { it.id })
            assertEquals(emptyList<SessionEntity>(), dao.sessionHistory("").first())
            assertEquals(listOf("builtin"), dao.observeDecks("").first().map { it.id })
        } finally {
            database.close()
        }
    }
}
