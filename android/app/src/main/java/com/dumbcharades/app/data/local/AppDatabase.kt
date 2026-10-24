package com.dumbcharades.app.data.local

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Entity(tableName = "decks", primaryKeys = ["id"])
data class DeckEntity(
    val id: String,
    val title: String,
    val description: String,
    val version: Int,
    val updatedAt: String,
    val cardCount: Int,
    val ageRestricted: Boolean,
    val featuredRank: Int?,
    val custom: Boolean,
    val deleted: Boolean,
    val payloadJson: String,
    val syncPending: Boolean,
    @ColumnInfo(defaultValue = "''") val ownerId: String = "",
)

@Entity(tableName = "sessions", primaryKeys = ["id"])
data class SessionEntity(val id: String, val payloadJson: String, val completed: Boolean, val updatedAt: String, val syncPending: Boolean, @ColumnInfo(defaultValue = "''") val ownerId: String = "")

@Dao
interface GameDao {
    @Query("UPDATE decks SET ownerId = :uid, syncPending = 1 WHERE custom = 1 AND ownerId = ''")
    suspend fun adoptGuestDecks(uid: String)

    @Query("UPDATE sessions SET ownerId = :uid, syncPending = 1 WHERE ownerId = ''")
    suspend fun adoptGuestSessions(uid: String)

    @Transaction
    suspend fun adoptGuestData(uid: String) {
        require(uid.isNotBlank())
        adoptGuestDecks(uid)
        adoptGuestSessions(uid)
    }

    @Query("SELECT * FROM decks WHERE deleted = 0 AND (custom = 0 OR ownerId = :uid) ORDER BY featuredRank IS NULL, featuredRank, title")
    fun observeDecks(uid: String): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    suspend fun deck(id: String): DeckEntity?

    @Query("SELECT * FROM decks WHERE title = :title AND deleted = 0 AND custom = 0 LIMIT 1")
    suspend fun deckByTitle(title: String): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecks(items: List<DeckEntity>)

    @Query("SELECT * FROM decks WHERE syncPending = 1 AND ownerId = :uid")
    suspend fun pendingDecks(uid: String): List<DeckEntity>

    @Query("UPDATE decks SET syncPending = 0 WHERE id = :id AND ownerId = :uid AND payloadJson = :payload")
    suspend fun markDeckSynced(id: String, uid: String, payload: String)

    @Transaction
    suspend fun mergeRemoteDeck(row: DeckEntity) {
        val local = deck(row.id)
        if (local == null || (!local.syncPending && local.ownerId == row.ownerId)) upsertDecks(listOf(row))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(item: SessionEntity)

    @Query("SELECT * FROM sessions WHERE completed = 0 AND ownerId = :uid ORDER BY updatedAt DESC LIMIT 1")
    suspend fun unfinishedSession(uid: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE syncPending = 1 AND ownerId = :uid")
    suspend fun pendingSessions(uid: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun session(id: String): SessionEntity?

    @Query("UPDATE sessions SET syncPending = 0 WHERE id = :id AND ownerId = :uid AND payloadJson = :payload")
    suspend fun markSessionSynced(id: String, uid: String, payload: String)

    @Transaction
    suspend fun mergeRemoteSession(row: SessionEntity) {
        val local = session(row.id)
        val newer = local == null || runCatching { Instant.parse(row.updatedAt) > Instant.parse(local.updatedAt) }.getOrDefault(false)
        if (local == null || (!local.syncPending && local.ownerId == row.ownerId && newer)) upsertSession(row)
    }

    @Query("SELECT * FROM sessions WHERE ownerId = :uid ORDER BY updatedAt DESC LIMIT 100")
    fun sessionHistory(uid: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE completed = 1 AND ownerId = :uid ORDER BY updatedAt DESC")
    fun completedSessions(uid: String): Flow<List<SessionEntity>>
}

@Database(entities = [DeckEntity::class, SessionEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { abstract fun gameDao(): GameDao }
