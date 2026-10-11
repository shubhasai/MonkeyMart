package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.ui.CheckoutViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class CheckoutViewModelTest {
    @Test
    fun networkFailure_surfacesErrorInsteadOfCrashing() = runBlocking {
        val vm = CheckoutViewModel()

        vm.submit { throw IOException("no network") }

        assertNotNull(vm.error)
        assertNull(vm.orderId)
        assertFalse(vm.placing)
    }

    @Test
    fun success_setsOrderIdAndClearsError() = runBlocking {
        val vm = CheckoutViewModel()
        vm.submit { throw IOException("no network") }

        vm.submit { "MM-000001" }

        assertEquals("MM-000001", vm.orderId)
        assertNull(vm.error)
        assertFalse(vm.placing)
    }
}
