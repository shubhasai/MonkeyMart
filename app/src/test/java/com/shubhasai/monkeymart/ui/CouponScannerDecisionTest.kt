package com.shubhasai.monkeymart.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CouponScannerDecisionTest {
    @Test
    fun opensWhenPermissionGranted() {
        assertEquals(ScanAction.Open, scanAction { true })
    }

    @Test
    fun requestsPermissionAgainAfterRevocation() {
        var granted = true
        assertEquals(ScanAction.Open, scanAction { granted })
        granted = false
        assertEquals(ScanAction.RequestPermission, scanAction { granted })
    }
}
