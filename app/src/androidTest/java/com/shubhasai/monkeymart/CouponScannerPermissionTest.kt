package com.shubhasai.monkeymart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Run with CAMERA revoked (fresh install or `pm revoke`); revoking in-process would kill the test.
@RunWith(AndroidJUnit4::class)
class CouponScannerPermissionTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun scanCouponAfterCameraRevoked_doesNotCrashAndDoesNotOpenScanner() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assumeTrue(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED,
        )
        // Stale "granted" flag as old builds left behind after the user revoked CAMERA in Settings.
        context.getSharedPreferences("monkeymart_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("camera_granted", true).commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(10_000) { count("product_1") > 0 }
            composeRule.onNodeWithTag("product_1").performClick()
            composeRule.waitUntil(10_000) { count("buy_now") > 0 }
            composeRule.onNodeWithTag("buy_now").performClick()
            composeRule.waitUntil(10_000) { count("scan_coupon") > 0 }

            composeRule.onNodeWithTag("scan_coupon").performClick()
            composeRule.waitForIdle()

            composeRule.onAllNodes(hasTestTag("coupon_status") and hasText("Scanner ready", substring = true))
                .assertCountEquals(0)
        }
    }

    private fun count(tag: String): Int =
        composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().size
}
