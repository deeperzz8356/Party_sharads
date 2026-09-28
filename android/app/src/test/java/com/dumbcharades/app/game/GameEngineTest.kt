package com.dumbcharades.app.game

import com.dumbcharades.app.model.GameMode
import com.dumbcharades.app.model.GameSession
import com.dumbcharades.app.model.GameType
import com.dumbcharades.app.model.MatchSettings
import com.dumbcharades.app.model.Prompt
import com.dumbcharades.app.model.PromptStatus
import com.dumbcharades.app.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    private val engine = GameEngine(Random(1))
    private val teams = listOf(Team(id = "a", name = "A", players = listOf("Asha")), Team(id = "b", name = "B", players = listOf("Ben")))
    private val prompts = listOf(Prompt(id = "1", text = "Titanic"), Prompt(id = "2", text = "Jaws"), Prompt(id = "3", text = "Rocky"), Prompt(id = "4", text = "Frozen"))

    @Test fun correct_and_penalty_change_score_while_skip_does_not() {
        var runtime = engine.start(GameSession(settings = MatchSettings(teams = teams)), prompts, 0)
        runtime = engine.answer(runtime, PromptStatus.GUESSED, 1_000)
        runtime = engine.answer(runtime, PromptStatus.SKIPPED, 2_000)
        runtime = engine.answer(runtime, PromptStatus.PENALTY, 3_000)
        assertEquals(0, runtime.score)
        assertEquals(3, runtime.results.size)
    }

    @Test fun skip_limit_blocks_extra_skips() {
        var runtime = engine.start(GameSession(settings = MatchSettings(skipLimit = 1, teams = teams)), prompts)
        runtime = engine.answer(runtime, PromptStatus.SKIPPED)
        runtime = engine.answer(runtime, PromptStatus.SKIPPED)
        assertEquals(1, runtime.results.size)
    }

    @Test fun teams_rotate_and_match_finishes_after_complete_round_cycles() {
        var session = GameSession(settings = MatchSettings(mode = GameMode.CLASSIC_TOUCH, totalRounds = 1, teams = teams))
        var runtime = engine.start(session, prompts)
        session = engine.finishRound(runtime)
        assertEquals(1, session.activeTeamIndex)
        assertFalse(session.completed)
        runtime = engine.start(session, prompts)
        session = engine.finishRound(runtime)
        assertEquals(0, session.activeTeamIndex)
        assertTrue(session.completed)
    }

    @Test fun manual_override_flips_result() {
        var runtime = engine.start(GameSession(settings = MatchSettings(teams = teams)), prompts)
        runtime = engine.answer(runtime, PromptStatus.SKIPPED)
        runtime = engine.override(runtime, 0)
        assertEquals(PromptStatus.GUESSED, runtime.results.single().status)
    }

    @Test fun bomb_uses_an_unpredictable_30_to_90_second_fuse() {
        val session = GameSession(settings = MatchSettings(gameType = GameType.BOMB_DEFUSAL, teams = teams))
        val runtime = engine.start(session, prompts)
        assertTrue(runtime.durationSeconds in 30..90)
        assertEquals(runtime.durationSeconds, runtime.remainingSeconds)
    }

    @Test fun playlist_rotates_after_each_full_team_cycle() {
        val settings = MatchSettings(playlist = listOf(GameType.CHARADES, GameType.IMPOSTOR_MIME), totalRounds = 2, teams = teams)
        var session = GameSession(settings = settings)
        assertEquals(GameType.CHARADES, session.activeGameType)
        session = engine.finishRound(engine.start(session, prompts))
        session = engine.finishRound(engine.start(session, prompts))
        assertEquals(GameType.IMPOSTOR_MIME, session.activeGameType)
    }

    @Test fun bomb_explosion_adds_a_penalty_to_the_holder() {
        val runtime = engine.start(GameSession(settings = MatchSettings(gameType = GameType.BOMB_DEFUSAL, teams = teams)), prompts)
        val exploded = engine.bombExploded(runtime.copy(remainingSeconds = 0))
        assertEquals(PromptStatus.PENALTY, exploded.results.single().status)
        assertEquals(-1, exploded.score)
    }
}

