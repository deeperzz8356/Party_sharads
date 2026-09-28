package com.dumbcharades.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun app_leaves_splash_and_first_screen_responds() {
        compose.waitUntil(10_000) {
            runCatching {
                compose.onAllNodes(
                    hasText("SIGN IN") or hasText("HOW TO PLAY") or hasText("DUMB CHARADES")
                ).fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
        }

        if (compose.onAllNodes(hasText("New here? Create an account")).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("New here? Create an account").performClick()
            compose.onNodeWithText("CREATE ACCOUNT", useUnmergedTree = true).assertExists()
        }
    }
}
