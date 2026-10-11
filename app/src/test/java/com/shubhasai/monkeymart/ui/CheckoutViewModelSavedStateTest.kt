package com.shubhasai.monkeymart.ui

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutViewModelSavedStateTest {
    @Test
    fun formSurvivesProcessDeath() {
        val handle = SavedStateHandle()
        val original = CheckoutViewModel(handle).apply {
            name = "Ravi Kumar"
            email = "ravi@example.com"
            phone = "9876543210"
            address = "12 MG Road"
            city = "Bengaluru"
            pincode = "560001"
        }

        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })
        val restored = CheckoutViewModel(restoredHandle)

        assertEquals(original.name, restored.name)
        assertEquals("Ravi Kumar", restored.name)
        assertEquals("ravi@example.com", restored.email)
        assertEquals("9876543210", restored.phone)
        assertEquals("12 MG Road", restored.address)
        assertEquals("Bengaluru", restored.city)
        assertEquals("560001", restored.pincode)
        assertTrue(restored.isComplete)
    }
}
