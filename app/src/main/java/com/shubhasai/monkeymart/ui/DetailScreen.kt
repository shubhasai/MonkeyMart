package com.shubhasai.monkeymart.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.data.ProductRepository
import com.shubhasai.monkeymart.data.dealPricePaise
import com.shubhasai.monkeymart.data.dispatchLabel
import com.shubhasai.monkeymart.data.freeDeliveryShortfallPaise
import com.shubhasai.monkeymart.data.priceLabel
import com.shubhasai.monkeymart.data.rupees
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Today's deal for a product, shown as a popup. */
class DealDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Deal unlocked")
            .setMessage(requireArguments().getString(ARG_MESSAGE))
            .setPositiveButton("Nice", null)
            .create()

    companion object {
        private const val ARG_MESSAGE = "message"
        fun newInstance(message: String) = DealDialogFragment().apply { arguments = bundleOf(ARG_MESSAGE to message) }
    }
}

/** Loads a product's detail in [viewModelScope]; the loaded id is saved so a restored screen shows content at once. */
class DetailViewModel(private val handle: SavedStateHandle) : ViewModel() {
    var product by mutableStateOf<Product?>(null)
        private set
    var pendingDeal by mutableStateOf<String?>(null)
        private set

    private var loading: Job? = null

    fun load(productId: Int) {
        if (product?.id == productId || loading?.isActive == true) return
        if (handle.get<Int>(LOADED_KEY) == productId) {
            product = ProductRepository.cached(productId)
            if (product != null) return
        }
        loading = viewModelScope.launch {
            val loaded = ProductRepository.byId(productId)
            product = loaded
            handle[LOADED_KEY] = productId
            if (loaded.dealPercent > 0 && handle.get<Int>(DEAL_SHOWN_KEY) != productId) {
                pendingDeal = "${loaded.dealPercent}% off ${loaded.name}, today only."
            }
        }
    }

    fun dealShown() {
        product?.let { handle[DEAL_SHOWN_KEY] = it.id }
        pendingDeal = null
    }

    private companion object {
        const val LOADED_KEY = "loadedProductId"
        const val DEAL_SHOWN_KEY = "dealShownProductId"
    }
}

@Composable
fun DetailScreen(productId: Int, onBuyNow: (Int) -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalContext.current as FragmentActivity
    val vm: DetailViewModel = viewModel(key = "detail-$productId")
    LaunchedEffect(productId) { vm.load(productId) }

    val deal = vm.pendingDeal
    LaunchedEffect(deal) {
        if (deal == null) return@LaunchedEffect
        activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val fm = activity.supportFragmentManager
            if (!fm.isStateSaved) {
                DealDialogFragment.newInstance(deal).show(fm, "deal")
                vm.dealShown()
            }
        }
    }

    val loaded = vm.product
    if (loaded == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("detail_loading"))
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).testTag("detail_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(loaded.emoji, fontSize = 96.sp)
        Text(loaded.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("detail_name"))
        Text(rupees(loaded.dealPricePaise()), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("detail_price"))
        if (loaded.dealPercent > 0) {
            Text("${loaded.dealPercent}% off today, was ${loaded.priceLabel()}", style = MaterialTheme.typography.bodySmall)
        }
        Text(loaded.dispatchLabel(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("dispatch"))
        val shortfall = loaded.freeDeliveryShortfallPaise()
        Text(
            if (shortfall == 0L) "Free delivery" else "Add ${rupees(shortfall)} more for free delivery",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("free_delivery"),
        )
        Text(loaded.description, style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = { onBuyNow(loaded.id) },
            modifier = Modifier.fillMaxWidth().testTag("buy_now"),
        ) { Text("Buy now") }
    }
}
