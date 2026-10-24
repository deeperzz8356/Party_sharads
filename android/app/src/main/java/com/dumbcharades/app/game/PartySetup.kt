package com.dumbcharades.app.game

import com.dumbcharades.app.model.Team

fun partyTeams(players: List<String>, twoTeams: Boolean, assignments: Map<String, Int> = emptyMap(), names: List<String> = emptyList()): List<Team> {
    val clean = players.map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }
    require(clean.isNotEmpty()) { "Add player names first" }
    if (!twoTeams) return listOf(Team(name = "Everyone", players = clean))
    require(clean.all { (assignments[it] ?: 0) in 0..1 }) { "Invalid team assignment" }
    val groups = (0..1).map { group -> clean.filterIndexed { index, player -> (assignments[player] ?: (index % 2)) == group } }
    require(groups.all { it.isNotEmpty() }) { "Each team needs at least one player" }
    return groups.mapIndexed { index, group -> Team(name = names.getOrNull(index)?.trim()?.take(30)?.takeIf { it.isNotBlank() } ?: if (index == 0) "Team Blue" else "Team Coral", players = group) }
}
