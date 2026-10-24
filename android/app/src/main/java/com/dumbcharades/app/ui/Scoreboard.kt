package com.dumbcharades.app.ui

import com.dumbcharades.app.model.GameSession

data class SavedTeamScore(val name: String, val players: List<String>, val points: Int)
data class SavedMatchScore(val teams: List<SavedTeamScore>, val result: String)

/** Historical scores come from the recorded rounds, never the current setup. */
fun GameSession.savedScore(): SavedMatchScore {
    val teams = settings.teams.map { team ->
        SavedTeamScore(team.name, team.players, rounds.filter { it.activeTeamId == team.id }.sumOf { it.score })
    }
    val result = when {
        !completed -> "In progress"
        rounds.isEmpty() -> "No scored rounds"
        teams.size == 1 -> "Group score"
        teams.isEmpty() -> "Match finished"
        else -> {
            val best = teams.maxOf { it.points }
            val leaders = teams.filter { it.points == best }
            if (leaders.size > 1) "It's a tie" else "${leaders.single().name} won"
        }
    }
    return SavedMatchScore(teams, result)
}
