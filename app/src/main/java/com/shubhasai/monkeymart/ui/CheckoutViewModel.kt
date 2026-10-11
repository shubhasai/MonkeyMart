package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRequest
import com.shubhasai.monkeymart.data.OrderSubmitter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/** Holds the checkout form; survives rotation (ViewModel) and process death (SavedStateHandle). */
class CheckoutViewModel(private val handle: SavedStateHandle) : ViewModel() {
    var name by saved("name")
    var email by saved("email")
    var phone by saved("phone")
    var address by saved("address")
    var city by saved("city")
    var pincode by saved("pincode")

    var placing by mutableStateOf(false)
        private set
    var orderId by mutableStateOf(handle.get<String>(KEY_ORDER_ID))
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val isComplete: Boolean
        get() = listOf(name, email, phone, address, city, pincode).all { it.isNotBlank() }

    fun placeOrder(orders: OrderSubmitter, productId: Int) {
        if (placing) return
        placing = true
        error = null
        val request = OrderRequest(productId, name, email, phone, address, city, pincode)
        viewModelScope.launch {
            try {
                val id = orders.submit(request)
                handle[KEY_ORDER_ID] = id
                orderId = id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "Couldn't place your order. Check your connection and try again."
            } finally {
                placing = false
            }
        }
    }

    private fun saved(key: String) = SavedString(handle, key)

    private class SavedString(private val handle: SavedStateHandle, private val key: String) {
        private var state by mutableStateOf(handle.get<String>(key) ?: "")

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = state

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state = value
            handle[key] = value
        }
    }

    private companion object {
        const val KEY_ORDER_ID = "order_id"
    }
}
