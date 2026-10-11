package com.shubhasai.monkeymart

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailRotationTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun rotatingWhileDealLoads_doesNotCrashAndStillShowsDeal() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithTagCount("product_1") > 0
            }
            composeRule.onNodeWithTag("product_1").performClick()

            scenario.recreate()

            composeRule.waitUntil(20_000) {
                composeRule.onAllNodesWithTagCount("detail_name") > 0
            }
            composeRule.onNodeWithTag("detail_name").assertIsDisplayed()
            composeRule.onNodeWithText("10% off Banana Bunch, today only.").assertIsDisplayed()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.onAllNodesWithTagCount(tag: String): Int =
        onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().size
}
