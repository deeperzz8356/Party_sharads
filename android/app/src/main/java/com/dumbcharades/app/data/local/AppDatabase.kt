package com.dumbcharades.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

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
)

@Entity(tableName = "sessions", primaryKeys = ["id"])
data class SessionEntity(val id: String, val payloadJson: String, val completed: Boolean, val updatedAt: String, val syncPending: Boolean)

@Dao
interface GameDao {
    @Query("SELECT * FROM decks WHERE deleted = 0 ORDER BY featuredRank IS NULL, featuredRank, title")
    fun observeDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    suspend fun deck(id: String): DeckEntity?

    @Query("SELECT * FROM decks WHERE title = :title AND deleted = 0 LIMIT 1")
    suspend fun deckByTitle(title: String): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecks(items: List<DeckEntity>)

    @Query("SELECT * FROM decks WHERE syncPending = 1")
    suspend fun pendingDecks(): List<DeckEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(item: SessionEntity)

    @Query("SELECT * FROM sessions WHERE completed = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun unfinishedSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE syncPending = 1")
    suspend fun pendingSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE completed = 1 ORDER BY updatedAt DESC")
    fun completedSessions(): Flow<List<SessionEntity>>
}

@Database(entities = [DeckEntity::class, SessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { abstract fun gameDao(): GameDao }
