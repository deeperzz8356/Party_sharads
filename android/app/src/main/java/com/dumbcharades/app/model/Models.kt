package com.dumbcharades.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable enum class GameMode { FOREHEAD_TILT, CLASSIC_TOUCH }
@Serializable enum class GameType { CHARADES, IMPOSTOR_MIME, BLANK_AGENT, INSIDE_VOICE, CROWD_TRAP, SIGNAL_LINE, TRUTH_OR_DARE, MOST_LIKELY, NEVER_HAVE_I_EVER, FAKE_IT, DRAW_AND_GUESS, WOULD_YOU_RATHER, TWIN_WORD_SPY, REVERSE_CHARADES, MIME_TELEPHONE, SOUND_EFFECTS, BOMB_DEFUSAL }
@Serializable enum class PromptStatus { GUESSED, SKIPPED, PENALTY }
@Serializable enum class Difficulty { EASY, MEDIUM, HARD }

@Serializable
data class Prompt(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val hint: String? = null,
)

@Serializable
data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val version: Int = 1,
    @SerialName("updated_at") val updatedAt: String = "1970-01-01T00:00:00Z",
    @SerialName("card_count") val cardCount: Int = 0,
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("age_restricted") val ageRestricted: Boolean = false,
    @SerialName("featured_rank") val featuredRank: Int? = null,
    val cards: List<Prompt> = emptyList(),
    val custom: Boolean = false,
    val deleted: Boolean = false,
)

@Serializable data class Team(val id: String = UUID.randomUUID().toString(), val name: String, val players: List<String>, val score: Int = 0)
@Serializable data class PromptResult(val promptId: String, val text: String, val status: PromptStatus, val elapsedSeconds: Float)
@Serializable
data class RoundResult(
    val index: Int,
    val activeTeamId: String,
    val actorName: String,
    val score: Int,
    val prompts: List<PromptResult>,
    val gameType: GameType = GameType.CHARADES,
)

@Serializable
data class MatchSettings(
    val gameType: GameType = GameType.CHARADES,
    val playlist: List<GameType> = emptyList(),
    val mode: GameMode = GameMode.CLASSIC_TOUCH,
    val timerSeconds: Int = 60,
    val totalRounds: Int? = 5,
    val skipLimit: Int? = 3,
    val penaltyEnabled: Boolean = false,
    val deckIds: List<String> = emptyList(),
    val teams: List<Team> = listOf(Team(name = "Mute Mimes", players = listOf("Player 1")), Team(name = "Silent Ninjas", players = listOf("Player 2"))),
)

@Serializable
data class GameSession(
    val id: String = UUID.randomUUID().toString(),
    val settings: MatchSettings,
    val currentRound: Int = 0,
    val activeTeamIndex: Int = 0,
    val rounds: List<RoundResult> = emptyList(),
    val completed: Boolean = false,
    val updatedAt: String = "1970-01-01T00:00:00Z",
) {
    val activeGameType: GameType
        get() = settings.playlist.takeIf { it.isNotEmpty() }
            ?.let { it[currentRound % it.size] }
            ?: settings.gameType
}

data class GameRuntime(
    val session: GameSession,
    val prompts: List<Prompt>,
    val promptIndex: Int = 0,
    val remainingSeconds: Int = session.settings.timerSeconds,
    val durationSeconds: Int = session.settings.timerSeconds,
    val results: List<PromptResult> = emptyList(),
    val paused: Boolean = false,
    val startedAtMillis: Long = 0,
) {
    val currentPrompt: Prompt? get() = prompts.getOrNull(promptIndex % prompts.size.coerceAtLeast(1))
    val skipsUsed: Int get() = results.count { it.status == PromptStatus.SKIPPED }
    val score: Int get() = results.fold(0) { total, result ->
        total + when (result.status) {
            PromptStatus.GUESSED -> 1
            PromptStatus.PENALTY -> -1
            PromptStatus.SKIPPED -> 0
        }
    }
}
