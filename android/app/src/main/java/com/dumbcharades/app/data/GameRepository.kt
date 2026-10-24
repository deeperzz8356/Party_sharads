package com.dumbcharades.app.data

import com.dumbcharades.app.data.local.DeckEntity
import com.dumbcharades.app.data.local.GameDao
import com.dumbcharades.app.data.local.SessionEntity
import com.dumbcharades.app.model.Deck
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.Prompt
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dumbcharades.app.sync.SyncWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository @Inject constructor(
    private val dao: GameDao,
    private val json: Json,
    private val auth: AuthRepository,
    private val cloud: FirebaseSyncStore,
    private val workManager: WorkManager,
) {
    private val syncMutex = Mutex()
    private val currentUid: String get() = auth.user.value?.uid.orEmpty()
    val decks: Flow<List<Deck>> = auth.user.flatMapLatest { dao.observeDecks(it?.uid.orEmpty()) }
        .map { rows -> rows.map(::decodeDeck) }
        .flowOn(Dispatchers.Default)
    val history: Flow<List<GameSession>> = auth.user.flatMapLatest { dao.sessionHistory(it?.uid.orEmpty()) }
        .map { rows -> rows.mapNotNull { runCatching { json.decodeFromString<GameSession>(it.payloadJson) }.getOrNull() }
            .filter { it.completed || it.rounds.isNotEmpty() } }
        .flowOn(Dispatchers.Default)
    val stats: Flow<LocalStats> = auth.user.flatMapLatest { dao.completedSessions(it?.uid.orEmpty()) }.map { rows ->
        val sessions = rows.map { json.decodeFromString<GameSession>(it.payloadJson) }
        val results = sessions.flatMap { session -> session.rounds.flatMap { it.prompts } }
        val guessed = results.count { it.status == com.dumbcharades.app.model.PromptStatus.GUESSED }
        val attempted = results.count { it.status != com.dumbcharades.app.model.PromptStatus.PENALTY }
        LocalStats(sessions.size, guessed, if (attempted == 0) 0 else guessed * 100 / attempted)
    }.flowOn(Dispatchers.Default)

    suspend fun ensureBundledDecks() {
        val missing = withContext(Dispatchers.Default) { BundledDecks.all() }
            .filter { dao.deck(it.id) == null }
            .map { it.toEntity(false) }
        if (missing.isNotEmpty()) dao.upsertDecks(missing)
    }

    suspend fun adoptDeviceData(uid: String) {
        if (uid.isNotBlank() && currentUid == uid) dao.adoptGuestData(uid)
    }

    suspend fun refresh(): Result<Unit> = syncResult {
        val uid = currentUid.takeIf { it.isNotBlank() } ?: return@syncResult
        syncPending().getOrThrow()
        cloud.download(uid, FirebaseSyncStore.DECKS).forEach { (id, payload) ->
            check(currentUid == uid) { "Account changed during sync" }
            val deck = runCatching { json.decodeFromString<Deck>(payload) }.getOrNull()
            if (deck != null && deck.id == id && deck.title.isNotBlank() && deck.cards.size in 1..500) {
                dao.mergeRemoteDeck(deck.copy(custom = true).toEntity(false, uid))
            }
        }
        cloud.download(uid, FirebaseSyncStore.SESSIONS).forEach { (id, payload) ->
            check(currentUid == uid) { "Account changed during sync" }
            val session = runCatching { json.decodeFromString<GameSession>(payload) }.getOrNull()
            if (session != null && session.id == id && session.isPlayable()) {
                dao.mergeRemoteSession(SessionEntity(id, payload, session.completed, session.updatedAt, false, uid))
            }
        }
    }

    suspend fun deck(id: String): Deck? = dao.deck(id)?.takeIf { !it.custom || it.ownerId == currentUid }?.let(::decodeDeck)

    suspend fun promptsFor(gameType: GameType, selectedIds: List<String>): List<Prompt> {
        val purposeBuiltTitle = when (gameType) {
            GameType.SOUND_EFFECTS -> "Sound Lab"
            GameType.BOMB_DEFUSAL -> "Five Second Frenzy"
            GameType.BLANK_AGENT, GameType.INSIDE_VOICE -> "Secret Words"
            GameType.CROWD_TRAP -> "Crowd Questions"
            GameType.SIGNAL_LINE -> "Signal Lines"
            GameType.MOST_LIKELY -> "Most Likely"
            GameType.NEVER_HAVE_I_EVER -> "Never Have I Ever"
            GameType.FAKE_IT -> "Fake It Trivia"
            GameType.DRAW_AND_GUESS -> "Draw & Guess"
            GameType.WOULD_YOU_RATHER -> "Would You Rather"
            GameType.TWIN_WORD_SPY -> "Twin Words"
            GameType.IMPOSTOR_MIME, GameType.MIME_TELEPHONE -> "Wild Scenes"
            else -> null
        }
        val purposeBuilt = purposeBuiltTitle?.let { dao.deckByTitle(it)?.let(::decodeDeck) }
        return purposeBuilt?.cards ?: selectedIds.mapNotNull { deck(it) }.flatMap { it.cards }
    }

    suspend fun saveCustomDeck(title: String, lines: List<String>): Deck {
        val normalized = lines.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
        require(title.isNotBlank()) { "Deck name is required" }
        require(title.trim().length <= 60) { "Deck name must be 60 characters or fewer" }
        require(normalized.isNotEmpty()) { "Add at least one prompt" }
        require(normalized.size <= 500) { "A deck can contain at most 500 prompts" }
        require(normalized.all { it.length <= 120 }) { "Each prompt must be 120 characters or fewer" }
        val deck = Deck(
            id = UUID.randomUUID().toString(), title = title.trim(), description = "Custom deck", cardCount = normalized.size,
            updatedAt = Instant.now().toString(), custom = true, cards = normalized.map { Prompt(text = it) },
        )
        dao.upsertDecks(listOf(deck.toEntity(true, currentUid)))
        scheduleSync()
        return deck
    }

    suspend fun saveSession(session: GameSession, pending: Boolean = true) {
        val owner = dao.session(session.id)?.ownerId?.takeIf { it.isNotBlank() } ?: currentUid
        // An in-progress round cannot migrate to another account after sign-out.
        if (owner != currentUid) return
        dao.upsertSession(SessionEntity(session.id, json.encodeToString(session), session.completed, session.updatedAt, pending, owner))
        if (pending) scheduleSync()
    }

    suspend fun restoreSession(): GameSession? = dao.unfinishedSession(currentUid)?.let { json.decodeFromString<GameSession>(it.payloadJson) }

    suspend fun syncPending(): Result<Unit> = syncResult {
        syncMutex.withLock {
            val uid = currentUid.takeIf { it.isNotBlank() } ?: return@withLock
            dao.adoptGuestData(uid)
            dao.pendingDecks(uid).forEach { row ->
                check(currentUid == uid) { "Account changed during sync" }
                cloud.upload(uid, FirebaseSyncStore.DECKS, row.id, row.payloadJson)
                dao.markDeckSynced(row.id, uid, row.payloadJson)
            }
            dao.pendingSessions(uid).forEach { row ->
                check(currentUid == uid) { "Account changed during sync" }
                cloud.upload(uid, FirebaseSyncStore.SESSIONS, row.id, row.payloadJson)
                dao.markSessionSynced(row.id, uid, row.payloadJson)
            }
        }
    }

    private fun scheduleSync() {
        if (currentUid.isBlank()) return
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        workManager.enqueueUniqueWork("game-sync-now", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private suspend fun syncResult(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (timeout: TimeoutCancellationException) {
        Result.failure(timeout)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun decodeDeck(row: DeckEntity): Deck = json.decodeFromString(row.payloadJson)
    private fun GameSession.isPlayable(): Boolean = settings.teams.isNotEmpty()
        && settings.teams.size <= 8
        && settings.teams.all { it.players.isNotEmpty() && it.name.isNotBlank() }
        && settings.teams.sumOf { it.players.size } in 2..20
        && activeTeamIndex in settings.teams.indices && currentRound >= 0
        && settings.timerSeconds in listOf(30, 60, 90, 120)
        && settings.totalRounds in listOf(null, 3, 5, 10)
        && settings.deckIds.isNotEmpty()

    private fun Deck.toEntity(pending: Boolean, owner: String = "") = DeckEntity(id, title, description, version, updatedAt, cardCount, ageRestricted,
        featuredRank, custom, deleted, json.encodeToString(this), pending, owner)

}

data class LocalStats(val gamesPlayed: Int = 0, val totalGuessed: Int = 0, val accuracyPercent: Int = 0)
