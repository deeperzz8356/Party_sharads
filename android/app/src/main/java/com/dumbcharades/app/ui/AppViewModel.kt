package com.dumbcharades.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dumbcharades.app.data.AppSettings
import com.dumbcharades.app.data.AuthRepository
import com.dumbcharades.app.data.AuthUser
import com.dumbcharades.app.data.GameRepository
import com.dumbcharades.app.data.LocalStats
import com.dumbcharades.app.data.SettingsStore
import com.dumbcharades.app.game.guide
import com.dumbcharades.app.game.GameEngine
import com.dumbcharades.app.model.Deck
import com.dumbcharades.app.model.GameMode
import com.dumbcharades.app.model.GameRuntime
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.MatchSettings
import com.dumbcharades.app.model.PromptStatus
import com.dumbcharades.app.model.Team
import com.dumbcharades.app.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AppUiState(
    val decks: List<Deck> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val matchSettings: MatchSettings = MatchSettings(),
    val session: GameSession? = null,
    val runtime: GameRuntime? = null,
    val loading: Boolean = true,
    val message: String? = null,
    val stats: LocalStats = LocalStats(),
    val history: List<GameSession> = emptyList(),
    val user: AuthUser? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: GameRepository,
    private val settingsStore: SettingsStore,
    private val workManager: WorkManager,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = combine(repository.decks, repository.stats, settingsStore.settings, mutable, authRepository.user) { decks, stats, settings, local, user ->
        local.copy(decks = decks.filter { !it.ageRestricted || settings.adultUnlocked }, settings = settings, stats = stats, loading = false, user = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())
    private val engine = GameEngine()
    private var timer: Job? = null

    init {
        viewModelScope.launch { repository.history.collect { history -> mutable.value = mutable.value.copy(history = history) } }
        viewModelScope.launch {
            repository.ensureBundledDecks()
            var initialized = false
            var previousUid: String? = null
            authRepository.user.collectLatest { user ->
                val activatingDevice = initialized && previousUid == null && user != null
                initialized = true
                previousUid = user?.uid
                if (user != null) repository.adoptDeviceData(user.uid)
                // Obtaining the first device identity must not interrupt a game
                // already running offline. Its guest rows have just been adopted.
                if (!activatingDevice) {
                    timer?.cancel()
                    mutable.value = mutable.value.copy(session = null, runtime = null, matchSettings = MatchSettings())
                }
                val restored = if (activatingDevice) mutable.value.session else repository.restoreSession()
                if (!activatingDevice) restored?.let {
                    mutable.value = mutable.value.copy(session = it, matchSettings = it.settings)
                }
                val restoredSettings = mutable.value.matchSettings
                if (user != null) {
                    repository.refresh().onFailure {
                        mutable.value = mutable.value.copy(message = "Offline mode: using downloaded decks")
                    }
                    if (!activatingDevice && authRepository.user.value?.uid == user.uid && mutable.value.runtime == null
                        && mutable.value.matchSettings == restoredSettings
                        && (mutable.value.session == null || mutable.value.session == restored)) {
                        repository.restoreSession()?.let {
                            mutable.value = mutable.value.copy(session = it, matchSettings = it.settings)
                        }
                    }
                }
            }
        }
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        workManager.enqueueUniquePeriodicWork("game-sync", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun completeOnboarding() = viewModelScope.launch { settingsStore.completeOnboarding() }
    fun unlockAdult(value: Boolean) = viewModelScope.launch { settingsStore.setAdultUnlocked(value) }
    fun setThreshold(value: Float) = viewModelScope.launch { settingsStore.setThreshold(value) }
    fun setTheme(value: String) = viewModelScope.launch { settingsStore.setTheme(value) }
    fun setSoundVolume(value: Float) = viewModelScope.launch { settingsStore.setSoundVolume(value) }
    fun setSoundPack(value: String) = viewModelScope.launch { settingsStore.setSoundPack(value) }
    fun dismissMessage() { mutable.value = mutable.value.copy(message = null) }
    fun chooseGame(gameType: GameType) {
        val inputMode = if (gameType == GameType.SOUND_EFFECTS) GameMode.FOREHEAD_TILT else GameMode.CLASSIC_TOUCH
        mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(gameType = gameType, playlist = emptyList(), mode = inputMode))
    }
    fun choosePlaylist(gameTypes: List<GameType>) {
        require(gameTypes.isNotEmpty())
        mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(gameType = gameTypes.first(), playlist = gameTypes, mode = GameMode.CLASSIC_TOUCH))
    }
    fun chooseMode(mode: GameMode) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(mode = mode)) }
    fun chooseDeck(id: String) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(deckIds = listOf(id))) }
    fun setTimer(seconds: Int) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(timerSeconds = seconds)) }
    fun setRounds(rounds: Int?) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(totalRounds = rounds)) }
    fun setSkipLimit(limit: Int?) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(skipLimit = limit)) }
    fun setPenaltyEnabled(enabled: Boolean) { mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(penaltyEnabled = enabled)) }
    fun setTeams(first: String, firstPlayers: String, second: String, secondPlayers: String) {
        val teams = listOf(Team(name = first.ifBlank { "Team A" }, players = parsePlayers(firstPlayers)), Team(name = second.ifBlank { "Team B" }, players = parsePlayers(secondPlayers)))
        mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(teams = teams))
    }
    fun setRoster(players: List<String>, useTeams: Boolean) {
        val clean = players.map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        val teams = if (useTeams && clean.size >= 2) {
            listOf(
                Team(name = "Blue Crew", players = clean.filterIndexed { index, _ -> index % 2 == 0 }),
                Team(name = "Pink Crew", players = clean.filterIndexed { index, _ -> index % 2 == 1 }),
            )
        } else listOf(Team(name = "The Party", players = clean))
        mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(teams = teams))
    }

    fun setTeams(teams: List<Team>) {
        require(teams.isNotEmpty() && teams.all { it.players.isNotEmpty() })
        val players = teams.flatMap { it.players }
        require(players.size <= 20 && players.all { it.isNotBlank() } && players.map { it.lowercase() }.distinct().size == players.size)
        mutable.value = mutable.value.copy(matchSettings = mutable.value.matchSettings.copy(teams = teams))
    }

    fun prepareMatch(): Boolean {
        val selected = mutable.value.matchSettings.deckIds
        if (selected.isEmpty()) { mutable.value = mutable.value.copy(message = "Choose a deck first"); return false }
        val settings = mutable.value.matchSettings
        val playerCount = settings.teams.sumOf { it.players.size }
        val activeModes = settings.playlist.ifEmpty { listOf(settings.gameType) }
        val required = activeModes.maxOf { it.guide().minimumPlayers }
        if (playerCount < required) {
            mutable.value = mutable.value.copy(message = "This playlist needs at least $required player names")
            return false
        }
        if (playerCount > 20) {
            mutable.value = mutable.value.copy(message = "Party modes currently support up to 20 players")
            return false
        }
        val session = GameSession(settings = settings, updatedAt = Instant.now().toString())
        mutable.value = mutable.value.copy(session = session, runtime = null)
        viewModelScope.launch { repository.saveSession(session) }
        return true
    }

    fun startRound() = viewModelScope.launch {
        val session = mutable.value.session ?: return@launch
        val prompts = repository.promptsFor(session.activeGameType, session.settings.deckIds)
        runCatching { engine.start(session, prompts) }
            .onSuccess { runtime -> mutable.value = mutable.value.copy(runtime = runtime); startTimer() }
            .onFailure { mutable.value = mutable.value.copy(message = it.message) }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            while ((mutable.value.runtime?.remainingSeconds ?: 0) > 0) {
                delay(1_000)
                mutable.value.runtime?.let { mutable.value = mutable.value.copy(runtime = engine.tick(it)) }
            }
        }
    }

    fun answer(status: PromptStatus) { mutable.value.runtime?.let { mutable.value = mutable.value.copy(runtime = engine.answer(it, status)) } }
    fun markBombExplosion() { mutable.value.runtime?.let { mutable.value = mutable.value.copy(runtime = engine.bombExploded(it)) } }
    fun pause() { mutable.value.runtime?.let { mutable.value = mutable.value.copy(runtime = engine.togglePause(it)) } }
    fun override(index: Int) {
        val oldRuntime = mutable.value.runtime ?: return
        val newRuntime = engine.override(oldRuntime, index)
        val session = mutable.value.session ?: return
        val delta = newRuntime.score - oldRuntime.score
        val lastRound = session.rounds.lastOrNull() ?: return
        val updatedTeams = session.settings.teams.map { if (it.id == lastRound.activeTeamId) it.copy(score = it.score + delta) else it }
        val updatedRounds = session.rounds.dropLast(1) + lastRound.copy(score = newRuntime.score, prompts = newRuntime.results)
        val updatedSession = session.copy(settings = session.settings.copy(teams = updatedTeams), rounds = updatedRounds, updatedAt = Instant.now().toString())
        mutable.value = mutable.value.copy(runtime = newRuntime, session = updatedSession)
        viewModelScope.launch { repository.saveSession(updatedSession) }
    }
    fun finishRound() = viewModelScope.launch {
        timer?.cancel()
        val runtime = mutable.value.runtime ?: return@launch
        val session = engine.finishRound(runtime)
        mutable.value = mutable.value.copy(session = session, runtime = runtime)
        repository.saveSession(session)
    }
    fun finishSpecialRound(won: Boolean) {
        timer?.cancel()
        val runtime = mutable.value.runtime ?: return
        val playable = runtime.copy(paused = false)
        val answered = engine.answer(playable, if (won) PromptStatus.GUESSED else PromptStatus.SKIPPED)
        val session = engine.finishRound(answered)
        mutable.value = mutable.value.copy(session = session, runtime = answered)
        viewModelScope.launch { repository.saveSession(session) }
    }
    fun nextRound() { mutable.value = mutable.value.copy(runtime = null) }
    fun rematch() {
        val previous = mutable.value.session ?: return
        val session = engine.rematch(previous)
        mutable.value = mutable.value.copy(session = session, matchSettings = session.settings, runtime = null)
        viewModelScope.launch { repository.saveSession(session) }
    }
    fun endMatch() = viewModelScope.launch {
        val session = mutable.value.session ?: return@launch
        val completed = session.copy(completed = true, updatedAt = Instant.now().toString())
        mutable.value = mutable.value.copy(session = completed, runtime = null)
        repository.saveSession(completed)
    }
    fun saveCustomDeck(title: String, raw: String, onSaved: (String) -> Unit) = viewModelScope.launch {
        val lines = raw.lineSequence().flatMap { line -> line.split(',').asSequence() }.toList()
        runCatching { repository.saveCustomDeck(title, lines) }
            .onSuccess { onSaved(it.id) }.onFailure { mutable.value = mutable.value.copy(message = it.message) }
    }

    private fun parsePlayers(value: String): List<String> = value.split(',').map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("Player") }
}
