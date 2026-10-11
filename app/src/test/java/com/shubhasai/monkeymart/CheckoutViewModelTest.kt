package com.shubhasai.monkeymart

import androidx.lifecycle.SavedStateHandle
import com.shubhasai.monkeymart.ui.CheckoutViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun formFields_surviveProcessDeath() {
        val handle = SavedStateHandle()
        val original = CheckoutViewModel(handle)
        original.name = "Ravi Kumar"
        original.email = "ravi@example.com"
        original.phone = "9876543210"
        original.address = "12 MG Road"
        original.city = "Bengaluru"
        original.pincode = "560001"

        val restored = CheckoutViewModel(
            SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        )

        assertEquals(original.name, restored.name)
        assertEquals(original.email, restored.email)
        assertEquals(original.phone, restored.phone)
        assertEquals(original.address, restored.address)
        assertEquals(original.city, restored.city)
        assertEquals(original.pincode, restored.pincode)
        assertTrue(restored.isComplete)
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
