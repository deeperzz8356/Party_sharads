package com.dumbcharades.app.data

import com.dumbcharades.app.data.local.DeckEntity
import com.dumbcharades.app.data.local.GameDao
import com.dumbcharades.app.data.local.SessionEntity
import com.dumbcharades.app.data.network.CustomDeckWrite
import com.dumbcharades.app.data.network.GameApi
import com.dumbcharades.app.data.network.NetworkGameWrite
import com.dumbcharades.app.data.network.NetworkPromptResult
import com.dumbcharades.app.data.network.NetworkRound
import com.dumbcharades.app.data.network.NetworkTeam
import com.dumbcharades.app.model.Deck
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(private val dao: GameDao, private val api: GameApi, private val json: Json) {
    val decks: Flow<List<Deck>> = dao.observeDecks()
        .map { rows -> rows.map(::decodeDeck) }
        .flowOn(Dispatchers.Default)
    val stats: Flow<LocalStats> = dao.completedSessions().map { rows ->
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

    suspend fun refresh(): Result<Unit> = runCatching {
        api.bootstrap()
        val summaries = api.decks().decks
        val fullDecks = summaries.map { summary -> api.deck(summary.id) }
        val custom = api.customDecks()
        dao.upsertDecks((fullDecks.map { it.copy(custom = false) } + custom.map { it.copy(custom = true) }).map { it.toEntity(false) })
    }

    suspend fun deck(id: String): Deck? = dao.deck(id)?.let(::decodeDeck)

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
        require(normalized.isNotEmpty()) { "Add at least one prompt" }
        require(normalized.size <= 500) { "A deck can contain at most 500 prompts" }
        val deck = Deck(
            id = UUID.randomUUID().toString(), title = title.trim(), description = "Custom deck", cardCount = normalized.size,
            updatedAt = Instant.now().toString(), custom = true, cards = normalized.map { Prompt(text = it) },
        )
        dao.upsertDecks(listOf(deck.toEntity(true)))
        return deck
    }

    suspend fun saveSession(session: GameSession, pending: Boolean = true) {
        dao.upsertSession(SessionEntity(session.id, json.encodeToString(session), session.completed, session.updatedAt, pending))
    }

    suspend fun restoreSession(): GameSession? = dao.unfinishedSession()?.let { json.decodeFromString<GameSession>(it.payloadJson) }

    suspend fun syncPending(): Result<Unit> = runCatching {
        dao.pendingDecks().forEach { row ->
            val deck = decodeDeck(row)
            if (!deck.deleted) api.createCustomDeck(CustomDeckWrite(deck.title, deck.cards, deck.id))
            dao.upsertDecks(listOf(row.copy(syncPending = false)))
        }
        dao.pendingSessions().forEach { row ->
            val session = json.decodeFromString<GameSession>(row.payloadJson)
            api.saveGame(session.toNetwork())
            dao.upsertSession(row.copy(syncPending = false))
        }
    }

    private fun decodeDeck(row: DeckEntity): Deck = json.decodeFromString(row.payloadJson)
    private fun Deck.toEntity(pending: Boolean) = DeckEntity(id, title, description, version, updatedAt, cardCount, ageRestricted,
        featuredRank, custom, deleted, json.encodeToString(this), pending)

    private fun GameSession.toNetwork() = NetworkGameWrite(
        id = id,
        mode = settings.mode.name,
        gameType = settings.gameType.name,
        playlist = settings.playlist.map { it.name },
        timerSeconds = settings.timerSeconds,
        totalRounds = settings.totalRounds,
        currentRound = currentRound,
        selectedDeckIds = settings.deckIds,
        teams = settings.teams.map { NetworkTeam(it.id, it.name, it.players, it.score) },
        rounds = rounds.map { round -> NetworkRound(round.index, round.activeTeamId, round.actorName, round.score,
            round.prompts.map { NetworkPromptResult(it.promptId, it.text, it.status.name, it.elapsedSeconds) }, round.gameType.name) },
        completed = completed,
        updatedAt = updatedAt,
    )
}

data class LocalStats(val gamesPlayed: Int = 0, val totalGuessed: Int = 0, val accuracyPercent: Int = 0)
