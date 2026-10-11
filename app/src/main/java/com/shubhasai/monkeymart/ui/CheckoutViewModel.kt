package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRepository
import com.shubhasai.monkeymart.data.OrderRequest
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Holds the checkout form while the user fills it in; form fields are backed by [SavedStateHandle] so they survive process death. */
class CheckoutViewModel(private val savedState: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    var name by savedField("name")
    var email by savedField("email")
    var phone by savedField("phone")
    var address by savedField("address")
    var city by savedField("city")
    var pincode by savedField("pincode")

    private fun savedField(key: String) = object : ReadWriteProperty<Any?, String> {
        private val state = mutableStateOf(savedState.get<String>(key) ?: "")

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = state.value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state.value = value
            savedState[key] = value
        }
    }

    var placing by mutableStateOf(false)
        private set
    var orderId by mutableStateOf<String?>(null)
        private set

    val isComplete: Boolean
        get() = listOf(name, email, phone, address, city, pincode).all { it.isNotBlank() }

    var error by mutableStateOf<String?>(null)
        private set

    fun placeOrder(orders: OrderRepository, productId: Int) {
        viewModelScope.launch {
            submit { orders.submit(OrderRequest(productId, name, email, phone, address, city, pincode)) }
        }
    }

    internal suspend fun submit(place: suspend () -> String) {
        placing = true
        error = null
        try {
            orderId = place()
        } catch (e: IOException) {
            error = "Couldn't place your order. Check your connection and try again."
        } finally {
            placing = false
        }
    }
}
