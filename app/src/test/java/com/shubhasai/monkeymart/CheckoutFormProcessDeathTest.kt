package com.shubhasai.monkeymart

import androidx.lifecycle.SavedStateHandle
import com.shubhasai.monkeymart.ui.CheckoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutFormProcessDeathTest {
    @Test
    fun formFields_surviveProcessDeath() {
        val handle = SavedStateHandle()
        val before = CheckoutViewModel(handle)
        before.name = "Ravi Kumar"
        before.email = "ravi@example.com"
        before.phone = "9876543210"
        before.address = "12 MG Road"
        before.city = "Bengaluru"
        before.pincode = "560001"

        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })
        val after = CheckoutViewModel(restoredHandle)

        assertEquals("Ravi Kumar", after.name)
        assertEquals("ravi@example.com", after.email)
        assertEquals("9876543210", after.phone)
        assertEquals("12 MG Road", after.address)
        assertEquals("Bengaluru", after.city)
        assertEquals("560001", after.pincode)
    }
}
