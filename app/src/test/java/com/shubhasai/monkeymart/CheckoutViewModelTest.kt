package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.ui.CheckoutViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class CheckoutViewModelTest {

    @Test
    fun placeOrder_whenNetworkIsDown_showsErrorAndStopsSpinner() {
        val vm = CheckoutViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))

        vm.placeOrder(1) { throw IOException("no network") }

        assertFalse(vm.placing)
        assertNull(vm.orderId)
        assertNotNull(vm.error)
    }

    @Test
    fun placeOrder_afterFailure_canSucceedOnRetry() {
        val vm = CheckoutViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))

        vm.placeOrder(1) { throw IOException("no network") }
        vm.placeOrder(1) { "MM-000001" }

        assertFalse(vm.placing)
        assertEquals("MM-000001", vm.orderId)
        assertNull(vm.error)
    }
}
