package com.shubhasai.monkeymart.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CouponScannerTest {

    @Test
    fun revokedPermissionNeverTouchesTheCamera() {
        var touchedCamera = false
        val results = mutableListOf<String>()

        CouponScanner.open(hasPermission = false, onResult = { results += it }) { touchedCamera = true }

        assertFalse(touchedCamera)
        assertEquals(listOf(CouponScanner.MSG_PERMISSION_NEEDED), results)
    }

    @Test
    fun securityExceptionFromCameraIsReportedNotThrown() {
        val results = mutableListOf<String>()

        CouponScanner.open(hasPermission = true, onResult = { results += it }) {
            throw SecurityException("Lacking privileges to access camera service")
        }

        assertEquals(listOf(CouponScanner.MSG_CAMERA_UNAVAILABLE), results)
    }

    @Test
    fun successfulOpenReportsItsResult() {
        val results = mutableListOf<String>()

        CouponScanner.open(hasPermission = true, onResult = { results += it }) { it("ready") }

        assertEquals(listOf("ready"), results)
    }
}
