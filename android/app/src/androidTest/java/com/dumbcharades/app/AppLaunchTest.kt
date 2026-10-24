package com.dumbcharades.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun exists(text: String) = runCatching {
        compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }.getOrDefault(false)

    private fun awaitText(text: String) {
        compose.waitUntil(15_000) { exists(text) }
    }

    private fun awaitDisplayed(text: String) {
        compose.waitUntil(15_000) { runCatching { compose.onNodeWithText(text).assertIsDisplayed(); true }.getOrDefault(false) }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        android.os.SystemClock.sleep(400)
        val directory = File(requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)), "ui-review")
        check(directory.exists() || directory.mkdirs())
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        try {
            File(directory, "$name.png").outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun selectTab(label: String) = compose.onNode(hasText(label) and hasClickAction()).performClick()

    private fun tapInList(text: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        compose.onNodeWithText(text).performClick()
    }
    private fun openCharades() {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Dumb Charades"))
        compose.onAllNodes(hasText("Dumb Charades") and hasClickAction()).onFirst().performClick()
    }
    private fun back() = compose.onNodeWithContentDescription("Back").performClick()

    @Test fun device_can_enter_games_without_login_and_play_a_round() {
        awaitText("Good times\nstart here.")
        org.junit.Assert.assertFalse(exists("SIGN IN"))
        org.junit.Assert.assertFalse(exists("PLAY OFFLINE AS GUEST"))
        capture("00-home")
        selectTab("Scores")
        awaitText("Keep the bragging rights.")
        capture("06-scoreboard")
        selectTab("Home")
        selectTab("My decks")
        awaitText("Make it\npersonal.")
        capture("05-my-decks")
        compose.onNodeWithText("Create a deck").performScrollTo().performClick()
        awaitText("New card deck")
        compose.onNodeWithText("Save deck").assertIsDisplayed()
        back()
        awaitText("Make it\npersonal.")
        selectTab("Home")
        tapInList("Browse all 17 games")
        awaitDisplayed("Act it out")
        capture("01-games")
        tapInList("Talk & bluff")
        awaitText("Social & secret roles")
        tapInList("Think & draw")
        awaitText("Words, trivia & creative")
        tapInList("Act it out")
        awaitText("Acting & performance")
        openCharades()
        awaitDisplayed("Next: add players")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("How to play"))
        capture("02-rules")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Hollywood Movies"))
        compose.onNodeWithText("Hollywood Movies").performClick()
        capture("03-packs")
        compose.onNodeWithText("Next: add players").performClick()
        awaitText("Who's playing?")
        capture("04-players")
        lateinit var vm: com.dumbcharades.app.ui.AppViewModel
        compose.runOnUiThread { vm = androidx.lifecycle.ViewModelProvider(compose.activity)[com.dumbcharades.app.ui.AppViewModel::class.java] }
        vm.state.value.matchSettings.teams.flatMap { it.players }.filter { it.startsWith("Smoke Test") }.distinct().forEach { player ->
            compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Remove $player"))
            compose.onNodeWithContentDescription("Remove $player").performClick()
        }
        listOf("Smoke Test Asha", "Smoke Test Ben").forEach { player ->
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Player name"))
            compose.onNodeWithText("Player name").performTextClearance()
            compose.onNodeWithText("Player name").performTextInput(player)
            compose.onNodeWithText("Add").performClick()
        }
        tapInList("Two teams")
        tapInList("Shuffle teams")
        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Move Smoke Test Asha to the other team"))
        compose.onNodeWithContentDescription("Move Smoke Test Asha to the other team").performClick()
        tapInList("Shuffle teams")
        capture("10-teams")
        tapInList("Game settings")
        tapInList("Tap buttons")
        compose.onNodeWithText("Start game").performClick()
        awaitText("Start the timer")
        capture("11-ready")
        compose.onNodeWithText("Start the timer").performClick()
        awaitText("Got it  ↓")
        compose.onNodeWithText("Got it  ↓").assertIsDisplayed().performClick()
        compose.onNodeWithText("Score: 1").assertIsDisplayed()
        capture("12-playing")
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Leave this round?").assertIsDisplayed()
        compose.onNodeWithText("Keep playing").performClick()
        compose.onNodeWithText("Score: 1").assertIsDisplayed()
        compose.onNodeWithText("Pause").performClick()
        compose.onNodeWithText("Quit to home").performClick()
        compose.onNodeWithText("Leave round").performClick()
        awaitText("Good times\nstart here.")
        compose.onNodeWithText("Continue Dumb Charades").performScrollTo().assertIsDisplayed().performClick()
        awaitText("Start the timer")
        compose.onNodeWithText("Back to home (game is saved)").performClick()
        awaitText("Good times\nstart here.")
        compose.runOnUiThread { vm.finishRound() }
        compose.waitUntil(15_000) { vm.state.value.session?.rounds?.isNotEmpty() == true }
        compose.runOnUiThread { vm.endMatch() }
        compose.waitUntil(15_000) { vm.state.value.history.any { it.id == vm.state.value.session?.id && it.completed } }
        selectTab("Scores")
        awaitText("Keep the bragging rights.")
        compose.waitUntil(15_000) { exists("1 pt") }
        capture("07-saved-scores")
        selectTab("Home")
    }
    @Test fun guidedChooserRecommendsOneFittingGameAndPreservesAnswersOnBack() {
        awaitText("Good times\nstart here.")
        tapInList("Help us pick a game")
        awaitText("Number of players")
        compose.onNodeWithText("Number of players").performTextClearance()
        compose.onNodeWithText("Number of players").performTextInput("2")
        compose.onNodeWithText("Next").performClick()
        awaitText("Laugh & act")
        compose.onNodeWithText("Next").performClick()
        awaitText("A quick round")
        compose.onNodeWithText("Find our game").performClick()
        awaitText("Your game pick")
        compose.onNodeWithText("Dumb Charades").assertIsDisplayed()
        capture("08-chooser-pick")
        tapInList("Show another")
        awaitDisplayed("Sound Effects Only")
        compose.onNodeWithText("Play this").performClick()
        awaitText("Next: add players")
        back()
        awaitText("Your game pick")
        awaitDisplayed("Sound Effects Only")
        repeat(4) { back() }
        awaitText("Good times\nstart here.")
    }

}
