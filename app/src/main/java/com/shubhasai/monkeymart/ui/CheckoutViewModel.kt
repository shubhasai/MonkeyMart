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
import kotlin.reflect.KProperty

private class SavedField(private val handle: SavedStateHandle, private val key: String) {
    private val state = mutableStateOf(handle.get<String>(key) ?: "")

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = state.value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        state.value = value
        handle[key] = value
    }
}

/** Holds the checkout form while the user fills it in; survives process death via [SavedStateHandle]. */
class CheckoutViewModel private constructor(
    handle: SavedStateHandle,
    scope: CoroutineScope?,
) : ViewModel() {
    constructor(handle: SavedStateHandle) : this(handle, null)
    constructor(
        scope: CoroutineScope?,
        handle: SavedStateHandle = SavedStateHandle(),
    ) : this(handle, scope)

    private val scope: CoroutineScope = scope ?: viewModelScope

    var name by SavedField(handle, "name")
    var email by SavedField(handle, "email")
    var phone by SavedField(handle, "phone")
    var address by SavedField(handle, "address")
    var city by SavedField(handle, "city")
    var pincode by SavedField(handle, "pincode")

    var placing by mutableStateOf(false)
        private set
    var orderId by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val isComplete: Boolean
        get() = listOf(name, email, phone, address, city, pincode).all { it.isNotBlank() }

    fun placeOrder(productId: Int, submit: suspend (OrderRequest) -> String) {
        placing = true
        error = null
        scope.launch {
            try {
                orderId = submit(OrderRequest(productId, name, email, phone, address, city, pincode))
            } catch (e: IOException) {
                error = "Couldn't place your order. Check your connection and try again."
            } finally {
                placing = false
            }
        }
    }
}
