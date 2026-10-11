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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.data.ProductRepository
import com.shubhasai.monkeymart.data.priceLabel

@Composable
fun DetailScreen(productId: Int, onBuyNow: (Int) -> Unit, modifier: Modifier = Modifier) {
    var product by remember { mutableStateOf<Product?>(null) }
    var dealMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(productId) {
        val loadedProduct = ProductRepository.byId(productId)
        product = loadedProduct
        if (loadedProduct.dealPercent > 0) {
            dealMessage = "${loadedProduct.dealPercent}% off ${loadedProduct.name}, today only."
        }
    }

    dealMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dealMessage = null },
            title = { Text("Deal unlocked") },
            text = { Text(message, modifier = Modifier.testTag("deal_text")) },
            confirmButton = { TextButton(onClick = { dealMessage = null }) { Text("Nice") } },
        )
    }

    val loaded = product
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
        Text(loaded.priceLabel(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("detail_price"))
        Text(loaded.description, style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = { onBuyNow(loaded.id) },
            modifier = Modifier.fillMaxWidth().testTag("buy_now"),
        ) { Text("Buy now") }
    }
}
