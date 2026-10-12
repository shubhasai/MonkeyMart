package com.shubhasai.monkeymart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** A loaded product and, if it has one, the text of today's deal popup. */
data class LoadedDeal(val product: Product, val dealMessage: String?)

/** Loads a product's detail and works out today's deal announcement. */
object DealLoader {
    suspend fun load(
        productId: Int,
        fetch: suspend (Int) -> Product = ProductRepository::byId,
    ): LoadedDeal {
        val product = fetch(productId)
        val message = if (product.dealPercent > 0) "${product.dealPercent}% off ${product.name}, today only." else null
        return LoadedDeal(product, message)
    }
}

@Composable
fun DetailScreen(productId: Int, onBuyNow: (Int) -> Unit, modifier: Modifier = Modifier) {
    var product by remember { mutableStateOf<Product?>(null) }
    var dealMessage by remember { mutableStateOf<String?>(null) }
    var dealDismissed by rememberSaveable(productId) { mutableStateOf(false) }
    LaunchedEffect(productId) {
        val result = DealLoader.load(productId)
        product = result.product
        dealMessage = result.dealMessage
    }

    val loaded = product
    if (loaded == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("detail_loading"))
        }
        return
    }

    val message = dealMessage
    if (message != null && !dealDismissed) {
        AlertDialog(
            onDismissRequest = { dealDismissed = true },
            title = { Text("Deal unlocked") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { dealDismissed = true }) { Text("Nice") } },
        )
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
