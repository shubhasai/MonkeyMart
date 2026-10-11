package com.shubhasai.monkeymart.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class CheckoutViewModelTest {

    @Test
    fun placeOrder_whenNetworkIsDown_showsErrorAndResetsPlacing() {
        var uncaught: Throwable? = null
        val scope = CoroutineScope(
            Dispatchers.Unconfined + kotlinx.coroutines.CoroutineExceptionHandler { _, e -> uncaught = e },
        )
        val vm = CheckoutViewModel(SavedStateHandle(), scope)
        vm.name = "A"
        vm.email = "a@b.c"
        vm.phone = "123"
        vm.address = "street"
        vm.city = "city"
        vm.pincode = "560001"

        vm.placeOrder({ throw IOException("no network") }, productId = 1)

        assertNull(uncaught)
        assertFalse(vm.placing)
        assertNull(vm.orderId)
        assertNotNull(vm.error)
        assertEquals("A", vm.name)
    }
}
