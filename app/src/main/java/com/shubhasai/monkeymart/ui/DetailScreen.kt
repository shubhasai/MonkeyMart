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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shubhasai.monkeymart.data.priceLabel

@Composable
fun DetailScreen(
    productId: Int,
    onBuyNow: (Int) -> Unit,
    modifier: Modifier = Modifier,
    vm: DetailViewModel = viewModel(key = "detail_$productId") {
        DetailViewModel(productId, createSavedStateHandle())
    },
) {
    val loaded = vm.product
    if (loaded == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (vm.loadFailed) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Couldn't load this product.", modifier = Modifier.testTag("detail_error"))
                    Button(onClick = { vm.load() }, modifier = Modifier.testTag("detail_retry")) { Text("Retry") }
                }
            } else {
                CircularProgressIndicator(Modifier.testTag("detail_loading"))
            }
        }
        return
    }

    if (loaded.dealPercent > 0 && !vm.dealDismissed) {
        AlertDialog(
            onDismissRequest = { vm.dismissDeal() },
            title = { Text("Deal unlocked") },
            text = { Text("${loaded.dealPercent}% off ${loaded.name}, today only.") },
            confirmButton = { TextButton(onClick = { vm.dismissDeal() }) { Text("Nice") } },
            modifier = Modifier.testTag("deal_dialog"),
        )
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
