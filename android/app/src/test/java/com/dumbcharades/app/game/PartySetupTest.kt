package com.dumbcharades.app.game

import com.dumbcharades.app.model.GameType
import org.junit.Assert.*
import org.junit.Test

class PartySetupTest {
    @Test fun recommendationsAlwaysFitEverySupportedPartySizeAndMood() {
        for (count in 2..20) for (mood in PartyMood.entries) for (quick in listOf(true, false)) {
            val picks = recommendGames(count, mood, quick)
            assertTrue(picks.isNotEmpty())
            assertTrue(picks.all { it.guide().minimumPlayers <= count && it.guide().mood == mood && (!quick || it.guide().quick) })
        }
        assertEquals(GameType.WOULD_YOU_RATHER, recommendGames(2, PartyMood.TALK, true).first())
        assertFalse(recommendGames(4, PartyMood.TALK, false).contains(GameType.INSIDE_VOICE))
    }
    @Test fun teamNamesAreOptionalAndOddGroupsKeepEveryone() {
        val teams = partyTeams(listOf("Asha", "Ben", "Cam", "Dee", "Eli"), true)
        assertEquals(listOf("Team Blue", "Team Coral"), teams.map { it.name })
        assertEquals(listOf(3, 2), teams.map { it.players.size })
        assertEquals(5, teams.flatMap { it.players }.distinct().size)
        assertEquals("Everyone", partyTeams(listOf(" Asha ", "asha", "Ben"), false).single().name)
        assertEquals(listOf("Asha", "Ben"), partyTeams(listOf(" Asha ", "asha", "Ben"), false).single().players)
    }
    @Test fun movesAndCustomNamesAreAppliedWithoutLosingPeople() {
        val teams = partyTeams(listOf("Asha", "Ben", "Cam"), true, mapOf("Asha" to 1, "Ben" to 0, "Cam" to 1), listOf(" Winners ", ""))
        assertEquals("Winners", teams[0].name)
        assertEquals("Team Coral", teams[1].name)
        assertEquals(listOf("Ben"), teams[0].players)
        assertEquals(listOf("Asha", "Cam"), teams[1].players)
    }
    @Test(expected = IllegalArgumentException::class) fun emptyTeamIsRejected() { partyTeams(listOf("Asha", "Ben"), true, mapOf("Asha" to 0, "Ben" to 0)) }
    @Test(expected = IllegalArgumentException::class) fun invalidAssignmentsCannotDropPlayers() { partyTeams(listOf("Asha", "Ben", "Cam"), true, mapOf("Cam" to 2)) }
}
