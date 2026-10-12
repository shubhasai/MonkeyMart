package com.shubhasai.monkeymart.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun ShopScreen(onProductClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    var products by remember { mutableStateOf<List<Product>?>(null) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { products = ProductRepository.all() }

    val list = products
    if (list == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("shop_loading"))
        }
        return
    }

    val shown = list.filter { it.name.contains(query.trim(), ignoreCase = true) }
    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search products") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag("shop_search"),
        )
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("shop_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(shown, key = { it.id }) { product ->
                ProductCard(product, onClick = { onProductClick(product.id) })
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
            .testTag("product_${product.id}"),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(product.emoji, fontSize = 32.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(product.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Text(product.priceLabel(), style = MaterialTheme.typography.titleMedium)
        }
    }
}
