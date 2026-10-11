package com.shubhasai.monkeymart.ui

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutViewModelStateTest {

    @Test
    fun formFields_areWrittenToSavedStateHandle() {
        val handle = SavedStateHandle()
        val vm = CheckoutViewModel(handle)

        vm.name = "Ravi Kumar"
        vm.pincode = "560001"

        assertEquals("Ravi Kumar", handle.get<String>("field_name"))
        assertEquals("560001", handle.get<String>("field_pincode"))
    }

    @Test
    fun newViewModelFromSameHandle_restoresFormState() {
        val handle = SavedStateHandle()
        val first = CheckoutViewModel(handle)
        first.name = "Ravi Kumar"
        first.email = "ravi@example.com"
        first.phone = "9876543210"
        first.address = "12 MG Road"
        first.city = "Bengaluru"
        assertFalse(first.isComplete)
        first.pincode = "560001"

        val restored = CheckoutViewModel(handle)

        assertEquals("Ravi Kumar", restored.name)
        assertTrue(restored.isComplete)
    }
}
