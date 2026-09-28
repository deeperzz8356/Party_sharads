package com.dumbcharades.app.game

import com.dumbcharades.app.model.GameRuntime
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.Prompt
import com.dumbcharades.app.model.PromptResult
import com.dumbcharades.app.model.PromptStatus
import com.dumbcharades.app.model.RoundResult
import java.time.Instant
import kotlin.random.Random

class GameEngine(private val random: Random = Random.Default) {
    fun start(session: GameSession, prompts: List<Prompt>, now: Long = System.currentTimeMillis()): GameRuntime {
        require(prompts.isNotEmpty()) { "The selected decks contain no prompts" }
        val duration = if (session.activeGameType == GameType.BOMB_DEFUSAL) random.nextInt(30, 91) else session.settings.timerSeconds
        val startsPaused = session.activeGameType !in setOf(GameType.CHARADES, GameType.SOUND_EFFECTS, GameType.BOMB_DEFUSAL)
        return GameRuntime(session, prompts.shuffled(random), remainingSeconds = duration, durationSeconds = duration, paused = startsPaused, startedAtMillis = now)
    }

    fun answer(runtime: GameRuntime, status: PromptStatus, now: Long = System.currentTimeMillis()): GameRuntime {
        val prompt = runtime.currentPrompt
        if (runtime.paused || runtime.remainingSeconds <= 0 || prompt == null) return runtime
        if (status == PromptStatus.SKIPPED && runtime.session.settings.skipLimit?.let { runtime.skipsUsed >= it } == true) return runtime
        val elapsed = ((now - runtime.startedAtMillis) / 1000f).coerceAtLeast(0f)
        val result = PromptResult(prompt.id, prompt.text, status, elapsed)
        return runtime.copy(promptIndex = runtime.promptIndex + 1, results = runtime.results + result)
    }

    fun tick(runtime: GameRuntime): GameRuntime = if (runtime.paused || runtime.remainingSeconds <= 0) runtime else runtime.copy(remainingSeconds = runtime.remainingSeconds - 1)
    fun togglePause(runtime: GameRuntime): GameRuntime = runtime.copy(paused = !runtime.paused)

    fun bombExploded(runtime: GameRuntime): GameRuntime {
        val prompt = runtime.currentPrompt ?: return runtime
        val penalty = PromptResult(prompt.id, "Bomb exploded: ${prompt.text}", PromptStatus.PENALTY, runtime.durationSeconds.toFloat())
        return runtime.copy(results = runtime.results + penalty)
    }

    fun override(runtime: GameRuntime, index: Int): GameRuntime {
        val old = runtime.results.getOrNull(index) ?: return runtime
        val replacement = old.copy(status = if (old.status == PromptStatus.GUESSED) PromptStatus.SKIPPED else PromptStatus.GUESSED)
        return runtime.copy(results = runtime.results.toMutableList().also { it[index] = replacement })
    }

    fun finishRound(runtime: GameRuntime): GameSession {
        val settings = runtime.session.settings
        val team = settings.teams[runtime.session.activeTeamIndex]
        val playerIndex = runtime.session.rounds.count { it.activeTeamId == team.id } % team.players.size
        val result = RoundResult(runtime.session.currentRound + 1, team.id, team.players[playerIndex], runtime.score, runtime.results, runtime.session.activeGameType)
        val updatedTeams = settings.teams.map { candidate -> if (candidate.id == team.id) candidate.copy(score = candidate.score + runtime.score) else candidate }
        val nextTeam = (runtime.session.activeTeamIndex + 1) % updatedTeams.size
        val nextRound = runtime.session.currentRound + if (nextTeam == 0) 1 else 0
        val complete = settings.totalRounds?.let { nextRound >= it } ?: false
        return runtime.session.copy(settings = settings.copy(teams = updatedTeams), currentRound = nextRound,
            activeTeamIndex = nextTeam, rounds = runtime.session.rounds + result, completed = complete, updatedAt = Instant.now().toString())
    }
}
