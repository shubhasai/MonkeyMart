package com.shubhasai.monkeymart

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailScreenRotationTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rotatingWhileDealLoadIsInFlightShowsDetailAndSingleDealPopup() {
        rule.waitUntil(10_000) { rule.onAllNodes(hasTestTag("product_1")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("product_1").performClick()
        Thread.sleep(1_200)

        rule.activityRule.scenario.recreate()

        rule.waitUntil(20_000) { rule.onAllNodes(hasTestTag("detail_name")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("detail_name").assertIsDisplayed()
        rule.waitUntil(20_000) { rule.onAllNodes(hasText("Deal unlocked")).fetchSemanticsNodes().isNotEmpty() }
        rule.waitForIdle()
        assertEquals(1, rule.onAllNodes(hasText("Deal unlocked")).fetchSemanticsNodes().size)
    }
}
