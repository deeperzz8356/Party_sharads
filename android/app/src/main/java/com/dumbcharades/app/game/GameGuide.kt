package com.dumbcharades.app.game

import com.dumbcharades.app.model.GameType

enum class PartyMood(val label: String) { ACT("Laugh & act"), TALK("Chat & bluff"), THINK("Think & draw") }
data class GameGuide(val minimumPlayers: Int, val mood: PartyMood, val easy: Boolean, val quick: Boolean, val premise: String, val example: String)

fun GameType.guide(): GameGuide = when (this) {
    GameType.CHARADES -> GameGuide(2, PartyMood.ACT, true, true, "One person acts. Everyone else guesses.", "If the card says 'brushing your teeth', mime it without speaking. The group guesses the action.")
    GameType.SOUND_EFFECTS -> GameGuide(2, PartyMood.ACT, true, true, "Make a sound. Your group guesses what it is.", "For 'a train', make a chugging sound. No spoken words or gestures.")
    GameType.REVERSE_CHARADES -> GameGuide(2, PartyMood.ACT, true, true, "Everyone acts together. One person guesses.", "The actors see 'rowing a boat' and mime together while the guesser tries to name it.")
    GameType.IMPOSTOR_MIME -> GameGuide(4, PartyMood.ACT, false, false, "Act together and find who never saw the card.", "The group mimes 'baking a cake'. The impostor copies them without knowing the prompt. Vote after acting.")
    GameType.MIME_TELEPHONE -> GameGuide(4, PartyMood.ACT, false, false, "Pass a scene through acting, drawing and guessing.", "A written scene becomes a silent video, then a drawing, then a guess. Compare the first and last versions.")
    GameType.WOULD_YOU_RATHER -> GameGuide(2, PartyMood.TALK, true, true, "Choose between two options, then compare answers.", "Would you rather fly or turn invisible? Choose privately, then reveal the room's choices.")
    GameType.TRUTH_OR_DARE -> GameGuide(2, PartyMood.TALK, true, true, "Pick truth or dare and follow the card.", "The spinner picks a player. They choose Truth, answer the question, then pass the phone.")
    GameType.MOST_LIKELY -> GameGuide(3, PartyMood.TALK, true, true, "Vote for the person who best fits the question.", "For 'most likely to miss a flight', everyone votes privately. Reveal who got the most votes.")
    GameType.NEVER_HAVE_I_EVER -> GameGuide(2, PartyMood.TALK, true, true, "Read a statement and admit whether you have done it.", "If 'fallen asleep in class' is true for you, tap your name and lose one heart.")
    GameType.BLANK_AGENT -> GameGuide(3, PartyMood.TALK, false, false, "Give clues and find the player with no secret word.", "Everyone sees 'beach' except the Agent. Give a clue like 'sand', then vote for the suspicious clue.")
    GameType.TWIN_WORD_SPY -> GameGuide(3, PartyMood.TALK, false, false, "Give clues and find who has a different word.", "Most players see 'coffee'; one sees 'tea'. Give short clues, then vote for the odd word.")
    GameType.INSIDE_VOICE -> GameGuide(5, PartyMood.TALK, false, false, "Solve a secret word while hidden roles help or mislead.", "Ask the Host yes/no questions about a secret word. After solving, vote for the hidden Distorter.")
    GameType.CROWD_TRAP -> GameGuide(4, PartyMood.TALK, false, false, "Answer, debate and try to agree with the crowd.", "Everyone names the best snack privately. Discuss the anonymous answers, then answer again.")
    GameType.SIGNAL_LINE -> GameGuide(4, PartyMood.THINK, false, false, "Agree on a position along a hidden number line.", "The Caller describes something on a cold-to-hot spectrum. Agree where it belongs and uncover the Jammer.")
    GameType.DRAW_AND_GUESS -> GameGuide(2, PartyMood.THINK, true, true, "One person draws. Everyone else guesses the word.", "For 'a bicycle', draw two wheels and a frame. Avoid writing letters; the group names the object.")
    GameType.BOMB_DEFUSAL -> GameGuide(2, PartyMood.THINK, true, true, "Name three things before time runs out.", "For 'three fruits', say apple, banana and mango, then pass the bomb before the timer ends.")
    GameType.FAKE_IT -> GameGuide(3, PartyMood.THINK, false, false, "Write a fake answer and fool the other players.", "Write a believable answer to an obscure question. The real answer is mixed with the fakes; everyone votes.")
}

fun recommendGames(players: Int, mood: PartyMood, quick: Boolean): List<GameType> {
    require(players in 2..20)
    val candidates = GameType.entries.filter { it.guide().minimumPlayers <= players && it.guide().mood == mood }
    val fitting = if (quick) candidates.filter { it.guide().quick } else candidates
    val firstPicks = listOf(GameType.CHARADES, GameType.WOULD_YOU_RATHER, GameType.DRAW_AND_GUESS, GameType.SOUND_EFFECTS, GameType.MOST_LIKELY, GameType.REVERSE_CHARADES, GameType.BOMB_DEFUSAL)
    return fitting.sortedWith(compareBy<GameType> { if (it.guide().easy) 0 else 1 }.thenBy { firstPicks.indexOf(it).takeIf { index -> index >= 0 } ?: 100 })
}
