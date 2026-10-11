package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRequest
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Holds the checkout form while the user fills it in. */
class CheckoutViewModel(private val handle: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private inner class SavedField(private val key: String) : ReadWriteProperty<Any?, String> {
        private var state by mutableStateOf(handle[key] ?: "")

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = state

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state = value
            handle[key] = value
        }
    }

    var name by SavedField("name")
    var email by SavedField("email")
    var phone by SavedField("phone")
    var address by SavedField("address")
    var city by SavedField("city")
    var pincode by SavedField("pincode")

    var placing by mutableStateOf(false)
        private set
    var orderId by mutableStateOf<String?>(null)
        private set

    val isComplete: Boolean
        get() = listOf(name, email, phone, address, city, pincode).all { it.isNotBlank() }

    var error by mutableStateOf<String?>(null)
        private set

    fun placeOrder(submit: suspend (OrderRequest) -> String, productId: Int) {
        viewModelScope.launch { submitOrder(submit, productId) }
    }

    internal suspend fun submitOrder(submit: suspend (OrderRequest) -> String, productId: Int) {
        placing = true
        error = null
        try {
            orderId = submit(OrderRequest(productId, name, email, phone, address, city, pincode))
        } catch (e: IOException) {
            error = "Couldn't place your order. Check your connection and try again."
        } finally {
            placing = false
        }
    }
}
