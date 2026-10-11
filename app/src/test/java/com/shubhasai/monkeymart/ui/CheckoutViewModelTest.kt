package com.shubhasai.monkeymart.ui

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class CheckoutViewModelTest {

    @Test
    fun networkFailureSurfacesErrorInsteadOfCrashing() {
        val vm = CheckoutViewModel().apply {
            name = "A"; email = "a@b.c"; phone = "1"; address = "x"; city = "y"; pincode = "1"
        }

        runBlocking {
            vm.submitOrder({ throw IOException("no network") }, productId = 1)
        }

        assertFalse(vm.placing)
        assertNull(vm.orderId)
        assertNotNull(vm.error)
    }
}
