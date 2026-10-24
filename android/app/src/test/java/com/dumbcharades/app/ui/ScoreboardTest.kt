package com.dumbcharades.app.ui

import com.dumbcharades.app.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreboardTest {
    private val teams = listOf(Team(id = "a", name = "A", players = listOf("Asha"), score = 99), Team(id = "b", name = "B", players = listOf("Ben"), score = 99))
    private fun round(team: String, score: Int) = RoundResult(0, team, "Player", score, emptyList())
    private fun match(rounds: List<RoundResult>, completed: Boolean = true) = GameSession(settings = MatchSettings(teams = teams), rounds = rounds, completed = completed)

    @Test fun totalsUseRecordedRoundsIncludingPenaltiesRatherThanSetupScores() {
        val score = match(listOf(round("a", 4), round("a", -1), round("b", 2))).savedScore()
        assertEquals(listOf(3, 2), score.teams.map { it.points })
        assertEquals("A won", score.result)
    }
    @Test fun equalScoresAndUnfinishedMatchesDoNotInventAWinner() {
        assertEquals("It's a tie", match(listOf(round("a", 2), round("b", 2))).savedScore().result)
        assertEquals("In progress", match(listOf(round("a", 10)), false).savedScore().result)
        assertEquals("No scored rounds", match(emptyList()).savedScore().result)
    }
    @Test fun oneGroupHasAGroupScore() {
        val session = GameSession(settings = MatchSettings(teams = teams.take(1)), rounds = listOf(round("a", 3)), completed = true)
        assertEquals("Group score", session.savedScore().result)
    }
}
