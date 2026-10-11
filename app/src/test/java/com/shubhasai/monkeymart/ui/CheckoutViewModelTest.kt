package com.shubhasai.monkeymart.ui

import androidx.lifecycle.SavedStateHandle
import com.shubhasai.monkeymart.data.OrderSubmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    /** What the OS does on process death: only the saved-state values come back, into a brand-new handle. */
    private fun SavedStateHandle.afterProcessDeath() =
        SavedStateHandle(keys().associateWith { get<Any>(it) })

    private fun CheckoutViewModel.fillForm() {
        name = "Asha"
        email = "asha@example.com"
        phone = "9999999999"
        address = "12 Banyan Road"
        city = "Pune"
        pincode = "411001"
    }

    @Test
    fun formSurvivesProcessDeath() {
        val handle = SavedStateHandle()
        CheckoutViewModel(handle).fillForm()

        val restored = CheckoutViewModel(handle.afterProcessDeath())

        assertEquals("Asha", restored.name)
        assertEquals("asha@example.com", restored.email)
        assertEquals("9999999999", restored.phone)
        assertEquals("12 Banyan Road", restored.address)
        assertEquals("Pune", restored.city)
        assertEquals("411001", restored.pincode)
        assertTrue(restored.isComplete)
    }

    @Test
    fun offlineSubmitDoesNotCrashAndKeepsTheForm() = runTest(dispatcher) {
        val vm = CheckoutViewModel(SavedStateHandle())
        vm.fillForm()

        vm.placeOrder({ throw IOException("no network") }, productId = 1)
        advanceUntilIdle()

        assertFalse(vm.placing)
        assertNull(vm.orderId)
        assertNotNull(vm.error)
        assertEquals("Asha", vm.name)
        assertEquals("411001", vm.pincode)
        assertTrue(vm.isComplete)
    }

    @Test
    fun retryAfterFailureSucceedsAndClearsError() = runTest(dispatcher) {
        val vm = CheckoutViewModel(SavedStateHandle())
        vm.fillForm()
        var online = false
        val orders = OrderSubmitter { if (online) "MM-000001" else throw IOException("no network") }

        vm.placeOrder(orders, 1)
        advanceUntilIdle()
        assertNotNull(vm.error)

        online = true
        vm.placeOrder(orders, 1)
        assertNull(vm.error)
        advanceUntilIdle()

        assertEquals("MM-000001", vm.orderId)
        assertNull(vm.error)
        assertFalse(vm.placing)
    }

    @Test
    fun placedOrderSurvivesProcessDeath() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val vm = CheckoutViewModel(handle)
        vm.fillForm()
        vm.placeOrder({ "MM-123456" }, 1)
        advanceUntilIdle()

        val restored = CheckoutViewModel(handle.afterProcessDeath())

        assertEquals("MM-123456", restored.orderId)
        assertEquals("Pune", restored.city)
    }

    @Test
    fun inFlightOrderIsNotSubmittedTwice() = runTest(dispatcher) {
        val vm = CheckoutViewModel(SavedStateHandle())
        vm.fillForm()
        var calls = 0
        val orders = OrderSubmitter {
            calls++
            delay(1_000)
            "MM-000002"
        }

        vm.placeOrder(orders, 1)
        vm.placeOrder(orders, 1)
        advanceUntilIdle()

        assertEquals(1, calls)
    }
}
