package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.OrderRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Holds the checkout form while the user fills it in. */
class CheckoutViewModel @JvmOverloads constructor(
    private val handle: SavedStateHandle,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val scope: CoroutineScope = scope ?: viewModelScope

    var name by savedField("field_name")
    var email by savedField("field_email")
    var phone by savedField("field_phone")
    var address by savedField("field_address")
    var city by savedField("field_city")
    var pincode by savedField("field_pincode")

    var placing by mutableStateOf(false)
        private set
    var orderId by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val isComplete: Boolean
        get() = listOf(name, email, phone, address, city, pincode).all { it.isNotBlank() }

    private fun savedField(key: String) = object : ReadWriteProperty<Any?, String> {
        private var state by mutableStateOf(handle.get<String>(key) ?: "")

        override fun getValue(thisRef: Any?, property: KProperty<*>): String = state

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state = value
            handle[key] = value
        }
    }

    fun placeOrder(submit: suspend (OrderRequest) -> String, productId: Int) {
        placing = true
        error = null
        scope.launch {
            try {
                orderId = submit(OrderRequest(productId, name, email, phone, address, city, pincode))
            } catch (e: IOException) {
                error = "No network, try again"
            } finally {
                placing = false
            }
        }
    }
}
