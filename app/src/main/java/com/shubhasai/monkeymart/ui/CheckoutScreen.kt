package com.shubhasai.monkeymart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shubhasai.monkeymart.data.OrderRepository
import com.shubhasai.monkeymart.data.ProductRepository
import com.shubhasai.monkeymart.data.priceLabel

@Composable
fun CheckoutScreen(
    productId: Int,
    modifier: Modifier = Modifier,
    vm: CheckoutViewModel = viewModel(),
) {
    val product = ProductRepository.cached(productId)
    val context = LocalContext.current
    val orders = remember { OrderRepository(context) }

    val placedId = vm.orderId
    if (placedId != null) {
        Column(modifier.fillMaxSize().padding(24.dp).testTag("order_confirmed"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order placed 🎉", style = MaterialTheme.typography.headlineSmall)
            Text("Order $placedId is on its way to ${vm.city}.", modifier = Modifier.testTag("order_id"))
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("checkout_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (product != null) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${product.emoji} ${product.name}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(product.priceLabel(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("checkout_total"))
                }
            }
        }

        Text("Delivery details", style = MaterialTheme.typography.titleMedium)
        CheckoutField("Full name", vm.name, { vm.name = it }, "field_name")
        CheckoutField("Email", vm.email, { vm.email = it }, "field_email", KeyboardType.Email)
        CheckoutField("Phone", vm.phone, { vm.phone = it }, "field_phone", KeyboardType.Phone)
        CheckoutField("Address", vm.address, { vm.address = it }, "field_address")
        CheckoutField("City", vm.city, { vm.city = it }, "field_city")
        CheckoutField("Pincode", vm.pincode, { vm.pincode = it }, "field_pincode", KeyboardType.Number)

        ScanCouponButton()

        vm.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("order_error"))
        }

        Button(
            onClick = { vm.placeOrder(productId, orders::submit) },
            enabled = vm.isComplete && !vm.placing,
            modifier = Modifier.fillMaxWidth().testTag("place_order"),
        ) {
            if (vm.placing) CircularProgressIndicator(Modifier.testTag("placing_order")) else Text("Place order")
        }
    }
}

@Composable
private fun CheckoutField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    tag: String,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}
