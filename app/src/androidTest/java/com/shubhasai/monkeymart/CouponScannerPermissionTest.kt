package com.shubhasai.monkeymart

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shubhasai.monkeymart.ui.CouponScanner
import com.shubhasai.monkeymart.ui.ScanCouponButton
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CouponScannerPermissionTest {
    @get:Rule
    val rule = createComposeRule()

    // Revoking a granted permission kills the instrumentation process, so this test runs
    // in the post-revocation state (CAMERA denied) with the stale grant flag left behind.
    @Test
    fun scanWithCameraDeniedAndStaleGrantDoesNotCrash() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("monkeymart_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("camera_granted", true).commit()
        assumeFalse(CouponScanner.hasCameraAccess(context))

        rule.setContent { ScanCouponButton() }
        rule.onNodeWithTag("scan_coupon").performClick()
        rule.waitForIdle()
    }
}
