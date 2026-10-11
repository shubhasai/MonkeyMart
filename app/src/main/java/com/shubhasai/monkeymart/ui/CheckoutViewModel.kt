package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRepository
import com.shubhasai.monkeymart.data.OrderRequest
import kotlinx.coroutines.launch

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

    fun placeOrder(orders: OrderRepository, productId: Int) {
        placing = true
        viewModelScope.launch {
            orderId = orders.submit(OrderRequest(productId, name, email, phone, address, city, pincode))
            placing = false
        }
    }
}
