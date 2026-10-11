package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.ui.CouponScanner
import org.junit.Assert.assertEquals
import org.junit.Test

class CouponScannerPermissionTest {
    @Test
    fun revokedPermission_requestsInsteadOfOpeningCamera() {
        val calls = mutableListOf<String>()
        CouponScanner.onScanClicked(
            hasCameraAccess = { false },
            open = { calls += "open" },
            requestPermission = { calls += "request" },
        )
        assertEquals(listOf("request"), calls)
    }

    @Test
    fun grantedPermission_opensCamera() {
        val calls = mutableListOf<String>()
        CouponScanner.onScanClicked(
            hasCameraAccess = { true },
            open = { calls += "open" },
            requestPermission = { calls += "request" },
        )
        assertEquals(listOf("open"), calls)
    }
}
