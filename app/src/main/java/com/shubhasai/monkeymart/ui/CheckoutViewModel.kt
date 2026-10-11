package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRepository
import com.shubhasai.monkeymart.data.OrderRequest
import kotlinx.coroutines.launch
import java.io.IOException

/** Holds the checkout form while the user fills it in. */
class CheckoutViewModel : ViewModel() {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var address by mutableStateOf("")
    var city by mutableStateOf("")
    var pincode by mutableStateOf("")

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
