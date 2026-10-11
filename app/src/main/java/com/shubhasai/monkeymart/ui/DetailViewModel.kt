package com.shubhasai.monkeymart.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.data.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Loads a product once (surviving rotation) and remembers whether the deal popup was dismissed. */
class DetailViewModel(
    private val productId: Int,
    private val handle: SavedStateHandle,
    private val loadProduct: suspend (Int) -> Product = ProductRepository::byId,
) : ViewModel() {
    var product by mutableStateOf<Product?>(null)
        private set
    var loadFailed by mutableStateOf(false)
        private set
    var dealDismissed by mutableStateOf(handle.get<Boolean>(KEY_DEAL_DISMISSED) ?: false)
        private set

    private var loading: Job? = null

    init {
        load()
    }

    fun load() {
        if (loading?.isActive == true) return
        loadFailed = false
        loading = viewModelScope.launch {
            try {
                product = loadProduct(productId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loadFailed = true
            }
        }
    }

    fun dismissDeal() {
        dealDismissed = true
        handle[KEY_DEAL_DISMISSED] = true
    }

    private companion object {
        const val KEY_DEAL_DISMISSED = "deal_dismissed"
    }
}
